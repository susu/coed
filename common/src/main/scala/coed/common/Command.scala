package coed.common

sealed trait Command {
  def position: Command.Position
}

final case class Insert(text: String, position: Command.Position) extends Command
final case class Delete(position: Command.Position, length: Int) extends Command

object Command {
  type Position = Int
}
