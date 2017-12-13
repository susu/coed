package coed.common

object Protocol {
  case object Join
  case class JoinSuccess(buffer: String, rev: Long)

  case class Edit(c: Command, rev: Long)
  case class Sync(c: Command, rev: Long)
}
