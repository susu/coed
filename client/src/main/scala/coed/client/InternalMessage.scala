package coed.client

object InternalMessage {
  sealed trait MoveCursor
  case object Left extends MoveCursor
  case object Right extends MoveCursor
  case object Down extends MoveCursor
  case object Up extends MoveCursor
  case object Top extends MoveCursor
  case object Bottom extends MoveCursor
  case object LineEnd extends MoveCursor
  case object LineStart extends MoveCursor

  sealed trait InsertMessage
  case class InsertText(text: String) extends InsertMessage
  case class InsertAfterText(text: String) extends InsertMessage

  case object Delete
  case object DeleteWord
  case object DeleteLine
  case object DeleteUntilEndOfLine

  case object SaveBuffer

  case class CommandBufferChanged(buffer: String)
  case class TextInsertBufferChanged(buffer: String)

  case class ChooseBuffer(index: Int)
}
