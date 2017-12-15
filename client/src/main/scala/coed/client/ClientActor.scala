package coed.client

import akka.actor.{Actor, ActorSelection}
import coed.common.{Delete, Insert}
import coed.common.Protocol._

class ClientActor(welcomeActor: ActorSelection, bufferUpdater: BufferUpdater) extends Actor {
  welcomeActor ! Join

  private var currentBufferId: Option[BufferId] = None

  override def receive: Receive = {
    case JoinSuccess(bufferList) =>
      printBufferList(bufferList)
      context.become(chooseBufferModeBehavior(bufferList))
  }

  private def handleSync: Receive = {
    case Sync(_, cmd, r) => bufferUpdater.syncBuffer(cmd, r)
  }

  private def chooseBufferModeBehavior(bufferList: List[BufferId]): Receive = {
    case KeyPressMessage(keypress) =>
      handleKeyPressInChooseBufferMode(bufferList, keypress)

    case OpenSuccess(buffer, rev) =>
      bufferUpdater.newBuffer(buffer, rev)
      context.become(normalModeBehavior.orElse(handleSync))
  }

  private def printBufferList(bufferList: List[BufferId]) = {
    println("Please choose a file to edit")
    bufferList.foreach(println)
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

    case Character('w') =>
      welcomeActor ! Persist(currentBufferId.get)

    case kp => Console.err.println(s"Unhandled input: $kp")
  }

  private def handleKeyPressInChooseBufferMode(bufferList: List[BufferId], keypress: KeyPress): Unit = keypress match {
    case Character(c) if c.isDigit =>
      val number = c.asDigit
      if (number > 0 && number <= bufferList.length) {
        currentBufferId = Some(bufferList(number-1))
        welcomeActor ! Open(currentBufferId.get)
      }
      else Console.err.println(s"Not a valid index $number")

    case Character('q') =>
      System.exit(0)

    case kp => Console.err.println(s"Unhandled input: $kp")
  }
}
