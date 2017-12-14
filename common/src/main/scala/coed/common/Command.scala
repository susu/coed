package coed.common

sealed trait Command
final case class Insert(text: String, position: Command.Position) extends Command
final case class Delete(position: Command.Position, length: Int) extends Command

object Command {
  type Position = Int
}

case class CommandMsg(cmd: Command)
