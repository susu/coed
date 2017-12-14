package coed.common

object Protocol {
  type BufferId = String

  case object Join
  case class JoinSuccess(bufferList: List[BufferId])

  case class Open(bufferId: BufferId)
  case class OpenSuccess(buffer: String, rev: Long)

  case class Edit(bufferId: BufferId, command: Command, rev: Long) //clientactor -> wactor

  case class Sync(bufferId: BufferId, command: Command, rev: Long) //bufferactor -> wactor -> clientactor
}
