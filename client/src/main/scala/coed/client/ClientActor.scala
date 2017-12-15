package coed.client

import akka.actor.{Actor, ActorSelection}
import coed.client.InternalMessage.{ChangeToInsertMode, ChangeToNormalMode}
import coed.common.Protocol._

class ClientActor(welcomeActor: ActorSelection, bufferUpdater: BufferUpdater) extends Actor {
  welcomeActor ! Join

  private val keypressHandler: KeypressHandler = new KeypressHandler(self, welcomeActor)

  override def receive: Receive = {
    case JoinSuccess(bufferList) =>
      printBufferList(bufferList)
      context.become(chooseBufferModeBehavior(bufferList))
  }

  private def chooseBufferModeBehavior(bufferList: List[BufferId]): Receive = {
    case KeyPressMessage(keypress) =>
      keypressHandler.handleKeyPressInChooseBufferMode(bufferList, keypress)

    case OpenSuccess(buffer, rev) =>
      bufferUpdater.newBuffer(buffer, rev)
      context.become(normalModeBehavior.orElse(handleSync))
  }

  private def normalModeBehavior: Receive = {
    case KeyPressMessage(keypress) =>
      keypressHandler.handleKeyPressInNormalMode(keypress)
    case ChangeToInsertMode =>
      context.become(insertModeBehavior.orElse(handleSync))
  }

  private def insertModeBehavior: Receive = {
    case KeyPressMessage(keyPress) =>
      keypressHandler.handleKeyPressInInsertMode(keyPress)
    case ChangeToNormalMode =>
      context.become(normalModeBehavior.orElse(handleSync))
  }

  private def handleSync: Receive = {
    case Sync(_, cmd, r) => bufferUpdater.syncBuffer(cmd, r)
  }

  private def printBufferList(bufferList: List[BufferId]) = {
    println("Please choose a file to edit")
    bufferList.zipWithIndex.foreach{
      case (bid, i) =>
        println(s"${i + 1}: $bid")
    }
  }
}
