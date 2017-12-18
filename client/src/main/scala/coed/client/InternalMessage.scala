package coed.client

object InternalMessage {
  case object ChangeToNormalMode
  case object ChangeToInsertMode

  sealed trait MoveCursor
  case object Left extends MoveCursor
  case object Right extends MoveCursor
  case object Down extends MoveCursor
  case object Up extends MoveCursor

  case class InsertText(text: String)
  case object Delete
  case object DeleteWord

  case class CommandBufferChanged(buffer: String)
  case class TextInsertBufferChanged(buffer: String)
}
