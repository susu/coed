package coed.common

sealed trait BufferError
final case object ApplicationFailure

trait Buffer {
  def applyCommand(command: Command): Either[BufferError, Buffer]
  def renderAll: String
  def render(start: Buffer.LineIndex, end: Buffer.LineIndex): String
  def start: Buffer.Position
  def end: Buffer.Position
  def numberOfLines: Int
}

object Buffer {
  case class Position(position: Int) extends AnyVal {
    def <(other: Position): Boolean = this.position < other.position
  }
  implicit val ordering = Ordering.by { pos: Position => pos.position }

  case class LineIndex(index: Int) extends AnyVal
  case class Line(line: String, lineIndex: LineIndex, start: Position)
}
