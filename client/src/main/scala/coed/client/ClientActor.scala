package coed.client

import akka.actor.{Actor, ActorSelection}
import coed.common.{Delete, Insert}
import coed.common.Protocol._

class ClientActor(welcomeActor: ActorSelection, bufferUpdater: BufferUpdater) extends Actor {
  welcomeActor ! Join

  private var currentBufferId: Option[BufferId] = None

  override def receive: Receive = {
    case JoinSuccess(bufferList) =>
      currentBufferId = Some(chooseBuffer(bufferList))
      welcomeActor ! Open(currentBufferId.get)

    case OpenSuccess(buffer, rev) =>
      bufferUpdater.newBuffer(buffer, rev)
      context.become(normalModeBehavior.orElse(handleSync))
  }

  private def handleSync: Receive = {
    case Sync(_, cmd, r) => bufferUpdater.syncBuffer(cmd, r)
  }

  private def insertModeBehavior: Receive = {
    case KeyPressMessage(keyPress) =>
      handleKeyPressInInsertMode(keyPress)
  }

  private def normalModeBehavior: Receive = {
    case KeyPressMessage(keypress) =>
      handleKeyPressInNormalMode(keypress)
  }

  private def handleKeyPressInInsertMode(keypress: KeyPress): Unit = keypress match {
    case Character(c) =>
      welcomeActor ! Edit(currentBufferId.get, Insert(c.toString, 0), 0)

    case Enter =>
      welcomeActor ! Edit(currentBufferId.get, Insert("\n\r", 0), 0)

    case Escape =>
      context.become(normalModeBehavior.orElse(handleSync))

    case kp => Console.err.println(s"Unhandled input: $kp")
  }

  private def handleKeyPressInNormalMode(keyPress: KeyPress): Unit = keyPress match {
    case Character('i') =>
      context.become(insertModeBehavior.orElse(handleSync))

    case Character('d') =>
      welcomeActor ! Edit(currentBufferId.get, Delete(0, 1), 0)

    case Character('q') =>
      System.exit(0)

    case kp => Console.err.println(s"Unhandled input: $kp")
  }


  private def chooseBuffer(ids: List[BufferId]): BufferId = ids.head //TODO proper choosing
}
