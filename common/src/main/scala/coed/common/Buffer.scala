package coed.common

sealed trait BufferError
final case object ApplicationFailure

trait Buffer {
  def applyCommand(command: Command): Either[BufferError, Buffer]
  def renderAll: String
  def render(start: Buffer.LineIndex, end: Buffer.LineIndex): Vector[Buffer.Line]
  def start: Buffer.Position
  def end: Buffer.Position
}

object Buffer {
  case class Position(position: Int) extends AnyVal {
    def <(other: Position): Boolean = this.position < other.position
    def +(other: Position): Position = Position(position + other.position)
    def add(i: Int): Position = Position(position + i)
  }
  implicit val ordering = Ordering.by { pos: Position => pos.position }

  case class LineIndex(index: Int) extends AnyVal {
    def moveUp: LineIndex = LineIndex(Math.max(0, index - 1))
    def moveDown: LineIndex = LineIndex(index + 1)
    def add(i: Int): LineIndex = LineIndex(index + i)
  }
  case class Line(line: String, lineIndex: LineIndex, start: Position)
}
