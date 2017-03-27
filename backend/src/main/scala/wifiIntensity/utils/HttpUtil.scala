package wifiIntensity.utils

import java.nio.charset.Charset

import akka.stream.Materializer
import org.asynchttpclient.{BoundRequestBuilder, DefaultAsyncHttpClient, Param, Response}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}

/**
  * User: Taoz
  * Date: 11/28/2016
  * Time: 17:28 PM
  */

object HttpUtil {
  private val ahClientImp: DefaultAsyncHttpClient = new DefaultAsyncHttpClient()
  private val log = LoggerFactory.getLogger(this.getClass)

  implicit class AhcToScala[T](reqBuilder: BoundRequestBuilder) {

    def scalaExecute(): Future[Response] = {
      import org.asynchttpclient.AsyncCompletionHandler
      val result = Promise[Response]()
      reqBuilder.execute(new AsyncCompletionHandler[Response]() {
        override def onCompleted(response: Response): Response = {
          result.success(response)
          response
        }
        override def onThrowable(t: Throwable): Unit = {
          result.failure(t)
        }
      })
      result.future
    }
  }
}

trait HttpUtil {


  import HttpUtil._

  implicit val executor: ExecutionContextExecutor

  implicit val materializer: Materializer

  private val ahClient: DefaultAsyncHttpClient = ahClientImp

  import collection.JavaConversions._

  private def parseResp(response: Response, charset: Charset) = {
    val body = new String(response.getResponseBodyAsBytes, charset)
    log.debug("getRequestSend response headers:" + response.getHeaders)
    log.debug("getRequestSend response body:" + body)
    if (response.getStatusCode != 200) {
      val uri = response.getUri
      val bodyLength = body.length
      val msg = s"getRequestSend http failed url = $uri, status = ${response.getStatusCode}, text = ${response.getStatusText}, body = ${body.substring(0, Math.min(bodyLength, 1024))}"
      log.warn(msg)
    }
    body
  }

  private def executeRequest(
                              methodName: String,
                              request: BoundRequestBuilder,
                              charset: Charset
                            ) = {
    request.scalaExecute().map { response =>
      Right(parseResp(response, charset))
    }.recover { case e: Throwable => Left(e) }
  }

  def postJsonRequestSend(
                           methodName: String,
                           url: String,
                           parameters: List[(String, String)],
                           jsonStr: String,
                           responseCharsetName: String = "UTF-8"
                         ): Future[Either[Throwable, String]] = {
    log.info("Post Request [" + methodName + "] Processing...")
    log.debug(methodName + " url=" + url)
    log.debug(methodName + " parameters=" + parameters)
    log.debug(methodName + " postData=" + jsonStr)
    val cs = Charset.forName(responseCharsetName)
    val request = ahClient.
      preparePost(url).
      setFollowRedirect(true).
      setRequestTimeout(20 * 1000).
      addQueryParams(parameters.map { kv => new Param(kv._1, kv._2) }).
      addHeader("Content-Type", "application/json").
      setBody(jsonStr).
      setCharset(cs)
    executeRequest(methodName, request, cs)
  }

  def getRequestSend(
                      methodName: String,
                      url: String,
                      parameters: List[(String, String)],
                      responseCharsetName: String = "UTF-8"
                    ): Future[Either[Throwable, String]] = {
    log.info("Get Request [" + methodName + "] Processing...")
    log.debug(methodName + " url=" + url)
    log.debug(methodName + " parameters=" + parameters)
    val request = ahClient.
      prepareGet(url).
      setFollowRedirect(true).
      setRequestTimeout(20 * 1000).
      addQueryParams(parameters.map { kv => new Param(kv._1, kv._2) })
    val cs = Charset.forName(responseCharsetName)
    executeRequest(methodName, request, cs)
  }

}