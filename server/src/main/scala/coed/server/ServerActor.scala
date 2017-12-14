package coed.server

import akka.actor.Actor
import coed.common.Protocol.{Edit, Join, JoinSuccess, Sync}
import coed.common.{Buffer, StringBuf}

class ServerActor(text: String) extends Actor {

  var buffer: Buffer = new StringBuf(text)

  override def receive = {
    case Join =>
      sender() ! JoinSuccess(buffer.render, 0)
    case Edit(c, _) =>
      buffer = buffer.applyCommand(c).getOrElse(buffer)
      context.parent ! Sync(c, 0)
  }
}
