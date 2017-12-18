package coed.client

import akka.actor.{FSM, ActorRef}

class KeypressHandlerActor(clientActor: ActorRef) extends FSM[KeypressHandlerActor.Mode, KeypressHandlerActor.InputBuffer] {
  import KeypressHandlerActor.{NormalMode, InsertMode, InputBuffer, MaxCommandLength}

  startWith(NormalMode, "")

  when(NormalMode) {
    case Event(KeyPressMessage(Character('i')), "") =>
      goto(InsertMode) using ""

    case Event(KeyPressMessage(Character('h')), "") =>
      clientActor ! InternalMessage.Left
      stay using ""

    case Event(KeyPressMessage(Character('j')), "") =>
      clientActor ! InternalMessage.Down
      stay using ""

    case Event(KeyPressMessage(Character('k')), "") =>
      clientActor ! InternalMessage.Up
      stay using ""

    case Event(KeyPressMessage(Character('l')), "") =>
      clientActor ! InternalMessage.Right
      stay using ""

    case Event(KeyPressMessage(Character('x')), "") =>
      clientActor ! InternalMessage.Delete
      stay using ""

    case Event(KeyPressMessage(Character('w')), "d") =>
      clientActor ! InternalMessage.DeleteWord
      clientActor ! InternalMessage.CommandBufferChanged("")
      stay using ""

    case Event(KeyPressMessage(Character(c)), inputBuffer) =>
      val newInputBuffer: InputBuffer = if (inputBuffer.length <= MaxCommandLength) inputBuffer + c else ""
      clientActor ! InternalMessage.CommandBufferChanged(newInputBuffer)
      stay using newInputBuffer
  }

  when(InsertMode) {
    case Event(Escape, inputBuffer) =>
      clientActor ! InternalMessage.InsertText(inputBuffer)
      clientActor ! InternalMessage.TextInsertBufferChanged("")
      goto(NormalMode) using ""

    case Event(keypress, inputBuffer) =>
      val newBuffer: InputBuffer = keypress match {
        case Character(c) => inputBuffer + c
        case Enter => inputBuffer ++ "\n"
        case Unknown(_) => inputBuffer
        case Escape => inputBuffer
      }
      clientActor ! InternalMessage.TextInsertBufferChanged(newBuffer)
      stay using newBuffer
  }

  initialize()
}


object KeypressHandlerActor {
  val MaxCommandLength: Int = 3

  sealed trait Mode
  case object InsertMode extends Mode
  case object NormalMode extends Mode

  type InputBuffer = String
}
