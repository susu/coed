package coed.common

sealed trait Command {
  def position: Buffer.Position
}

final case class Insert(text: String, position: Buffer.Position) extends Command
final case class Delete(position: Buffer.Position, length: Int) extends Command

