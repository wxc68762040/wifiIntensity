package wifiIntensity.http

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import org.slf4j.LoggerFactory
import wifiIntensity.common.Constants.Routes
import wifiIntensity.utils.{CirceSupport, SecureUtil}
import wifiIntensity.protocol.{ErrorCode, JsonProtocol, RequestWithData, RequestWithoutData}
import wifiIntensity.utils.AppClientKeys.{AppClientSecureKey, AskAppClientKey}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

/**
  * Created by liuziwei on 2016/7/4.
  */
trait BaseService extends JsonProtocol with SessionBase with CirceSupport{

  implicit val system: ActorSystem

  implicit def executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  implicit val timeout: Timeout

  val appClientKeys: ActorRef

  val boxManager: ActorRef
  
  private val log = LoggerFactory.getLogger("com.neo.sk.nyx.http.BaseService")

  lazy val regex = "(10.*|localhost|192.*|127.*)".r

  import io.circe._
  import io.circe.generic.auto._
  
  def ensureAuth(
                  appClientId: String,
                  timestamp: String,
                  nonce: String,
                  sn: String,
                  otherParams: List[String],
                  signature: String
                )(f: => Future[server.Route]) =
  {
    val p =
      getSecureKey(appClientId).flatMap {
        case Some(secureKey) =>
          val paramList = List(appClientId.toString, timestamp, nonce, sn) ::: otherParams
          if (timestamp.toLong + 120000 < System.currentTimeMillis()) {
            Future.successful(complete(ErrorCode.operationTimeOut))
          } else if (SecureUtil.checkSignature(paramList, signature, secureKey)) {
            f
          } else {
            Future.successful(complete(ErrorCode.signatureError))
          }
        case None =>
          Future.successful(complete(ErrorCode.appIdInvalid))
      }
    dealFutureResult(p)
  }

  def ensurePostAuth(
                      request: RequestWithData
                    )(f: => Future[server.Route]) =
    host(regex) { _ =>
      import request._
      val p =
        getSecureKey(appId).flatMap {
          case Some(secureKey) =>
            if (timestamp.toLong + 120000 < System.currentTimeMillis()) {
              log.error(s"request time to long.")
              Future.successful(complete(ErrorCode.operationTimeOut))
            } else if (SecureUtil.checkPostEnvelope(request, secureKey)) {
              f
            } else {
              log.error(s"signatureError.")
              Future.successful(complete(ErrorCode.signatureError))
            }
          case None =>
            log.error(s"app client not found.")
            Future.successful(complete(ErrorCode.appIdInvalid))
        }
      dealFutureResult(p)
    }

  def getSecureKey(appClientId: String) = {
    (appClientKeys ? AskAppClientKey(appClientId)).map {
      case AppClientSecureKey(key) => key
      case x => None
    }
  }


  def dealFutureResult(future: => Future[server.Route]) = {
    onComplete(future) {
      case Success(rst) => rst
      case Failure(e) =>
        e.printStackTrace()
        log.warn(s"internal error: ${e.getMessage}")
        complete(ErrorCode.internalError(e.getMessage))
    }
  }

  def authAndObtainPostParas(implicit f: Json => server.Route, isDataExist: Boolean = true) = {
    val r = extractRequestContext.map[server.Route] { ctx ⇒
      val prefix = ctx.request.uri.path.toString
      val isApi = prefix.startsWith(Routes.apiRoutes)
      val parseFmt = (isApi, isDataExist) match {
        case (true, true) =>
          as[Either[Error, RequestWithData]]
        case (true, false) =>
          as[Either[Error, RequestWithoutData]]
        case _ =>
          as[Either[Error, Json]]
      }
      import scala.util.{Failure, Success}
      val dataF = onComplete(parseFmt(ctx.request)).flatMap[Tuple1[Either[Error, Any]]] {
        case Success(value) ⇒
          log.info(s"BaseService obtainPostParas dataF:$value")
          provide(value)
        case Failure(e) ⇒
          log.info(s"BaseService obtainPostParas reject:${e.getMessage}")
          reject()
      }
      dataF {
        case Right(req) =>
          log.info(s"BaseService obtainPostParas req=$req")
          import cats.syntax.either._
          req match {
            case r: RequestWithData =>
              ensurePostAuth(r) {Future(f(io.circe.parser.parse(r.data).getOrElse(Json.Null)))}
            case r1: RequestWithoutData =>
              import r1._
              val r2 = RequestWithData(appId, nonce, timestamp, sn, signature, "")
              ensurePostAuth(r2) {Future(f(Json.Null))}
            case t: Json =>
              userAuth {
                case Some(_) => f(t)
                case None => redirect("/nyx2/admin/login", StatusCodes.SeeOther)
              }
          }
        case Left(e) =>
          log.error(s"BaseService obtainPostParas error:${e.getMessage}")
          complete(ErrorCode.requestIllegal(e.getMessage))
      }
    }
    r {r1 => r1}
  }

}