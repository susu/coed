package coed.common

sealed trait BufferError
final case object ApplicationFailure

trait Buffer {
  def applyCommand(command: Command): Either[BufferError, Buffer]
  def render: String
}
