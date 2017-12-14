package coed.client

import akka.actor.{Actor, ActorSelection}
import coed.common.CommandMsg
import coed.common.Protocol._

class ClientActor(welcomeActor: ActorSelection, bufferUpdater: BufferUpdater) extends Actor {
  welcomeActor ! Join

  var currentBufferId: Option[BufferId] = None

  override def receive = {

    case JoinSuccess(bufferList) =>
      currentBufferId = Some(chooseBuffer(bufferList))
      welcomeActor ! Open(currentBufferId.get)

    case OpenSuccess(buffer, rev) =>
      bufferUpdater.newBuffer(buffer, rev)

    case CommandMsg(cmd) => welcomeActor ! Edit(currentBufferId.get, cmd, 0)

    case Sync(_, cmd, r) => bufferUpdater.syncBuffer(cmd, r)
  }


  private def chooseBuffer(ids: List[BufferId]): BufferId = ids.head //TODO proper choosing
}
