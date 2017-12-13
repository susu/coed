package coed.common

class StringBuf(text: String) extends Buffer {
  def applyCommand(command: Command): Either[BufferError, Buffer] = command match {
    case Insert(newText, position) => {
      val (left, right) = text.splitAt(position)
      Right(new StringBuf(left ++ newText ++ right))
    }
    case Delete(position, length) => {
      val (left, right) = text.splitAt(position)
      Right(new StringBuf(left ++ right.drop(length)))
    }
  }
}
