package coed.common

sealed trait BufferError
final case object ApplicationFailure

trait Buffer {
  def applyCommand(command: Command): Either[BufferError, Buffer]
  def render: String
}

object Buffer {
  case class Position(position: Int) extends AnyVal {
    def <(other: Position): Boolean = this.position < other.position
  }
  implicit val ordering = Ordering.by { pos: Position => pos.position }
}
