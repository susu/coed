package coed.server

import akka.actor.Actor
import coed.common.Protocol._
import coed.common.{Buffer, StringBuf}

class BufferActor(text: String) extends Actor {

  var buffer: Buffer = new StringBuf(text)

  override def receive = {
    case Open(_) =>
      sender() ! OpenSuccess(buffer.render, 0)

    case Edit(bid, c, _) =>
      buffer = buffer.applyCommand(c).getOrElse(buffer)
      context.parent ! Sync(bid, c, 0)
  }
}
