package coed.client

import akka.actor.{ActorRef, ActorSelection}
import coed.client.InternalMessage.{ChangeToInsertMode, ChangeToNormalMode}
import coed.common.{Delete, Insert}
import coed.common.Protocol.{BufferId, Edit, Open, Persist}

class KeypressHandler(clientActor: ActorRef, welcomeActor: ActorSelection, renderer: Renderer) {
  private var currentBufferId: Option[BufferId] = None

  def handleKeyPressInChooseBufferMode(bufferList: List[BufferId], keypress: KeyPress): Unit = keypress match {
    case Character(c) if c.isDigit =>
      val number = c.asDigit
      if (number > 0 && number <= bufferList.length) {
        currentBufferId = Some(bufferList(number-1))
        welcomeActor.tell(Open(currentBufferId.get), clientActor)
      }
      else Console.err.println(s"Not a valid index $number")

    case Character('q') =>
      System.exit(0)

    case kp => Console.err.println(s"Unhandled input: $kp")
  }

  def handleKeyPressInNormalMode(keyPress: KeyPress): Unit = keyPress match {
    case Character('h') => renderer.moveLeft
    case Character('j') => renderer.moveDown
    case Character('k') => renderer.moveUp
    case Character('l') => renderer.moveRight

    case Character('i') =>
      clientActor ! ChangeToInsertMode

    case Character('d') =>
      welcomeActor.tell(Edit(currentBufferId.get, Delete(0, 1), 0), clientActor)

    case Character('q') =>
      System.exit(0)

    case Character('w') =>
      welcomeActor.tell(Persist(currentBufferId.get), clientActor)

    case kp => Console.err.println(s"Unhandled input: $kp")
  }

  def handleKeyPressInInsertMode(keypress: KeyPress): Unit = keypress match {
    case Character(c) =>
      welcomeActor.tell(Edit(currentBufferId.get, Insert(c.toString, 0), 0), clientActor)

    case Enter =>
      welcomeActor.tell(Edit(currentBufferId.get, Insert("\n\r", 0), 0), clientActor)

    case Escape =>
      clientActor ! ChangeToNormalMode

    case kp => Console.err.println(s"Unhandled input: $kp")
  }
}
