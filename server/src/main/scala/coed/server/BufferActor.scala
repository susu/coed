package coed.server

import akka.actor.Actor
import coed.common.Protocol._
import coed.common.{Buffer, StringBuf}
import coed.server.InternalMessage.PersistBuffer
import coed.server.persistence.BufferFile


class BufferActor(var filename: String, val workspaceDir: String) extends Actor {
  filename = workspaceDir + '/' + filename
  val bufferFile: BufferFile = new BufferFile(workspaceDir, filename)
  var buffer: Buffer = new StringBuf(bufferFile.load)

  override def receive = {
    case Open(_) =>
      sender() ! OpenSuccess(buffer.render, 0)

    case Edit(bid, c, _) =>
      buffer = buffer.applyCommand(c).getOrElse(buffer)
      context.parent ! Sync(bid, c, 0)

    case PersistBuffer =>
      bufferFile.persist(buffer.render)
  }
}
