package wifiIntensity.core

import akka.actor.ActorRef
import akka.event.{EventBus, SubchannelClassification}
import akka.util.Subclassification
import wifiIntensity.protocol.PutShoots

/**
  * Created by liuziwei on 2016/7/10.
  */
object DataBus{
  val ALL_CLASSIFY = "all"
  def getClassify(boxMac: String) = s"$ALL_CLASSIFY$boxMac"
}

class DataBus extends EventBus with SubchannelClassification{
  import DataBus._

  override type Event = (String, PutShoots)
  override type Classifier = String
  override type Subscriber = ActorRef

  override protected def publish(event: Event, subscriber: Subscriber): Unit = subscriber ! event._2

  override protected def classify(event: Event): Classifier = event._1

  override protected implicit def subclassification: Subclassification[Classifier] = Subclassification

  def subscribeAll(subscriber: Subscriber) = this.subscribe(subscriber, ALL_CLASSIFY)


  val Subclassification = new Subclassification[String] {
    override def isEqual(x: String, y: String): Boolean = y == x

    override def isSubclass(x: String, y: String): Boolean = y.startsWith(x)
  }

}
