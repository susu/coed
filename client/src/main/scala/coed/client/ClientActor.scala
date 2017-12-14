package coed.client

import akka.actor.{Actor, ActorSelection}
import coed.common.Protocol.{Edit, Join, JoinSuccess, Sync}

class ClientActor(server: ActorSelection, bufferUpdater: BufferUpdater) extends Actor {
  server ! Join

  override def receive = {
    case e: Edit => server ! e
    case JoinSuccess(b, r) => bufferUpdater.newBuffer(b, r)
    case Sync(c, r) => bufferUpdater.syncBuffer(c, r)
  }
}
