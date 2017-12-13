package coed.client

import akka.actor.{Actor, ActorRef}
import coed.common.Protocol.{Edit, Join, JoinSuccess, Sync}
import coed.common.{Buffer, StringBuf}

import scala.collection.mutable

class ServerActor(text: String) extends Actor {

  var buffer: Buffer = new StringBuf(text)
  val clients: mutable.HashSet[ActorRef] = new mutable.HashSet[ActorRef]()

  override def receive = {
    case Join =>
      clients.add(sender())
      sender() ! JoinSuccess(buffer.render, 0)
    case Edit(c, _) =>
      buffer = buffer.applyCommand(c).getOrElse(buffer)
      clients.foreach(_ ! Sync(c, 0))
  }
}
