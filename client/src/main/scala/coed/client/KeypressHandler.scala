package coed.client

import akka.actor.{ActorRef, ActorSelection}
import coed.client.InternalMessage.{ChangeToInsertMode, ChangeToNormalMode}
import coed.common.Delete
import coed.common.Protocol.{BufferId, Edit, Open, Persist}

class KeypressHandler(clientActor: ActorRef, welcomeActor: ActorSelection, bufferState: BufferState) {
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
    case Character('h') => bufferState.moveLeft
    case Character('j') => bufferState.moveDown
    case Character('k') => bufferState.moveUp
    case Character('l') => bufferState.moveRight

    case Character('i') =>
      clientActor ! ChangeToInsertMode

    case Character('a') =>
      clientActor ! ChangeToInsertMode
      bufferState.moveRight

    case Character('d') =>
      welcomeActor.tell(Edit(currentBufferId.get, Delete(bufferState.cursorPosition, 1), 0), clientActor)

    case Character('q') =>
      System.exit(0)

    case Character('w') =>
      welcomeActor.tell(Persist(currentBufferId.get), clientActor)

    case kp => Console.err.println(s"Unhandled input: $kp")
  }

  def handleKeyPressInInsertMode(keypress: KeyPress): Unit = keypress match {
    case Character(c@_) =>
      print("cica")
      // TODO renderer.insertIntoCurrentText(c)

    case Enter =>
      print("kutyus")
      // TODO renderer.insertIntoCurrentText('\n')

    case Escape =>
      // TODO welcomeActor.tell(Edit(currentBufferId.get, Insert(currentInsertedText.toString, renderer.cursorPosition), 0), clientActor)
      clientActor ! ChangeToNormalMode

    case kp => Console.err.println(s"Unhandled input: $kp")
  }
}
