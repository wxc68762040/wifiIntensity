package wifiIntensity.protocol

import akka.actor.ActorRef
import wifiIntensity.utils.Shoot

/**
	* Created by 流風幻葬 on 2017/3/27.
	*/
sealed trait ActorProtocol

case class PutShoots(boxMac: String, shoots: List[Shoot]) extends ActorProtocol
case class SubscribeData(peer: ActorRef, boxMac: String) extends ActorProtocol
