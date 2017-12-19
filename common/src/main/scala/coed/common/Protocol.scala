package coed.common

object Protocol {
  type BufferId = String

  case class Join(user: String)
  case class JoinSuccess(bufferList: List[BufferId])

  case class Open(bufferId: BufferId)
  case class OpenSuccess(buffer: String, rev: Long)

  case class Edit(bufferId: BufferId, command: Command, rev: Long) //clientactor -> wactor

  case class Sync(bufferId: BufferId, command: Command, rev: Long) //bufferactor -> wactor -> clientactor

  case class Persist(bufferId: BufferId)

  case class SyncUserList(bufferId: BufferId, users: List[String])

  case class CursorPositionUpdate(bufferId: BufferId, position: Int)
}
