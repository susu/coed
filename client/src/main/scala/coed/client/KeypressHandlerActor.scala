package coed.client

import akka.actor.{FSM, ActorRef}

class KeypressHandlerActor(clientActor: ActorRef) extends FSM[KeypressHandlerActor.Mode, KeypressHandlerActor.State] {
  import KeypressHandlerActor.{NormalMode, InsertMode, MaxCommandLength, NormalModeState, InsertModeState}

  startWith(NormalMode, NormalModeState(""))

  when(NormalMode) {
    case Event(KeyPressMessage(Character('i')), NormalModeState("")) =>
      goto(InsertMode) using InsertModeState((buffer : String) => InternalMessage.InsertText(buffer),"")

    case Event(KeyPressMessage(Character('a')), NormalModeState("")) =>
      goto(InsertMode) using InsertModeState((buffer : String) => InternalMessage.InsertAfterText(buffer),"")

    case Event(KeyPressMessage(Character('h')), NormalModeState("")) =>
      clientActor ! InternalMessage.Left
      stay using NormalModeState("")

    case Event(KeyPressMessage(Character('j')), NormalModeState("")) =>
      clientActor ! InternalMessage.Down
      stay using NormalModeState("")

    case Event(KeyPressMessage(Character('k')), NormalModeState("")) =>
      clientActor ! InternalMessage.Up
      stay using NormalModeState("")

    case Event(KeyPressMessage(Character('l')), NormalModeState("")) =>
      clientActor ! InternalMessage.Right
      stay using NormalModeState("")

    case Event(KeyPressMessage(Character('x')), NormalModeState("")) =>
      clientActor ! InternalMessage.Delete
      stay using NormalModeState("")

    case Event(KeyPressMessage(Character('q')), NormalModeState("")) =>
      System.exit(0)
      stay using NormalModeState("")

    case Event(KeyPressMessage(Character(c)), NormalModeState("v")) =>
      clientActor ! InternalMessage.ChooseBuffer(Option(c.toInt).getOrElse(0))
      stay using NormalModeState("")

    case Event(KeyPressMessage(Character('w')), NormalModeState("d")) =>
      clientActor ! InternalMessage.DeleteWord
      clientActor ! InternalMessage.CommandBufferChanged("")
      stay using NormalModeState("")

    case Event(KeyPressMessage(Character('w')), NormalModeState(":")) =>
      clientActor ! InternalMessage.SaveBuffer
      clientActor ! InternalMessage.CommandBufferChanged("")
      stay using NormalModeState("")

    case Event(KeyPressMessage(Character('g')), NormalModeState("g")) =>
      clientActor ! InternalMessage.Top
      clientActor ! InternalMessage.CommandBufferChanged("")
      stay using NormalModeState("")

    case Event(KeyPressMessage(Character('G')), NormalModeState("")) =>
      clientActor ! InternalMessage.Bottom
      clientActor ! InternalMessage.CommandBufferChanged("")
      stay using NormalModeState("")

    case Event(KeyPressMessage(Character('$')), NormalModeState("")) =>
      clientActor ! InternalMessage.LineEnd
      clientActor ! InternalMessage.CommandBufferChanged("")
      stay using NormalModeState("")

    case Event(KeyPressMessage(Character('|')), NormalModeState("")) =>
      clientActor ! InternalMessage.LineStart
      clientActor ! InternalMessage.CommandBufferChanged("")
      stay using NormalModeState("")

    case Event(KeyPressMessage(Character(c)), NormalModeState(inputBuffer)) =>
      val newInputBuffer: String = if (inputBuffer.length < (MaxCommandLength - 1)) inputBuffer + c else ""
      clientActor ! InternalMessage.CommandBufferChanged(newInputBuffer)
      stay using NormalModeState(newInputBuffer)

    case Event(_, _) =>
      stay using NormalModeState("")
  }

  when(InsertMode) {
    case Event(KeyPressMessage(Escape), InsertModeState(messageFactory, inputBuffer)) =>
      clientActor ! messageFactory(inputBuffer)
      clientActor ! InternalMessage.TextInsertBufferChanged("")
      goto(NormalMode) using NormalModeState("")

    case Event(KeyPressMessage(keypress), InsertModeState(messageFactory, inputBuffer)) =>
      val newBuffer: String = keypress match {
        case Character(c) => inputBuffer + c
        case Enter => inputBuffer ++ "\n"
        case _ => inputBuffer
      }
      clientActor ! InternalMessage.TextInsertBufferChanged(newBuffer)
      stay using InsertModeState(messageFactory, newBuffer)
  }

  initialize()
}


object KeypressHandlerActor {
  val MaxCommandLength: Int = 2

  sealed trait Mode
  case object InsertMode extends Mode
  case object NormalMode extends Mode

  sealed trait State
  case class NormalModeState(buffer: String) extends State
  case class InsertModeState(eventFactory: String => InternalMessage.InsertMessage, buffer: String) extends State
}
