package coed.common

class StringBuf(text: String) extends Buffer {
  override def applyCommand(command: Command): Either[BufferError, Buffer] = command match {
    case Insert(newText, Buffer.Position(position)) => {
      val (left, right) = text.splitAt(position)
      Right(new StringBuf(left ++ newText ++ right))
    }
    case Delete(Buffer.Position(position), length) => {
      val (left, right) = text.splitAt(position)
      Right(new StringBuf(left ++ right.drop(length)))
    }
  }

  override def render(start: Buffer.LineIndex, end: Buffer.LineIndex): String = ""
  override def renderAll: String = text
  override val start: Buffer.Position = Buffer.Position(0)
  override val end: Buffer.Position = Buffer.Position(text.length)
  override val numberOfLines: Int = 0
}
