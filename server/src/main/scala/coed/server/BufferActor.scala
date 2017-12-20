package coed.server

import akka.actor.Actor
import coed.common.Protocol._
import coed.common.{Buffer, StringBuf}
import coed.server.InternalMessage.PersistBuffer
import coed.server.persistence.Workspace


class BufferActor(val filename: BufferId, val workspace: Workspace) extends Actor {
  var buffer: Buffer = new StringBuf(workspace.load(filename))

  override def receive: Receive = {
    case Open(_) =>
      sender() ! OpenSuccess(buffer.renderAll, 0)

    case Edit(bid, c, _) =>
      buffer = buffer.applyCommand(c).getOrElse(buffer)
      context.parent ! Sync(bid, c, 0)

    case PersistBuffer =>
      workspace.persist(filename, buffer.renderAll)
  }
}
