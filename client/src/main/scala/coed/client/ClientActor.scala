package coed.client

import akka.actor.{Actor, ActorRef}
import coed.common.Protocol.{Edit, Join, JoinSuccess, Sync}
import coed.common.{Buffer, StringBuf}

class ClientActor(server: ActorRef) extends Actor {
  server ! Join

  var buffer: Option[Buffer] = None
  override def receive = {
    case e: Edit =>
      server ! e
    case JoinSuccess(b, _) => buffer = Some(new StringBuf(b))
    case Sync(c, _) => buffer = buffer.map(oldBuffer => oldBuffer.applyCommand(c).getOrElse(oldBuffer))
  }
}
