package coed.client

object InternalMessage {
  case object ChangeToNormalMode
  case object ChangeToInsertMode

  sealed trait MoveCursor
  case object Left extends MoveCursor
  case object Right extends MoveCursor
  case object Down extends MoveCursor
  case object Up extends MoveCursor

  sealed trait InsertMessage
  case class InsertText(text: String) extends InsertMessage
  case class InsertAfterText(text: String) extends InsertMessage

  case object Delete
  case object DeleteWord

  case class CommandBufferChanged(buffer: String)
  case class TextInsertBufferChanged(buffer: String)

  case class ChooseBuffer(index: Int)
}
