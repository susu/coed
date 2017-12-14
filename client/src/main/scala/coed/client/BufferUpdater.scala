package coed.client

import coed.common.{Buffer, StringBuf, Command}

trait BufferUpdater {
  def newBuffer(text: String, revision: Long): Unit
  def syncBuffer(command: Command, revision: Long): Unit
}

class SimpleBufferUpdater extends BufferUpdater {
  private var buffer: Option[Buffer] = None

  override def newBuffer(text: String, revision: Long): Unit = buffer = Some(new StringBuf(text))

  override def syncBuffer(command: Command, revision: Long): Unit = {
    buffer =
      buffer.map(oldBuffer => oldBuffer.applyCommand(command).getOrElse(oldBuffer))
      println(render)
  }

  private def render: String = buffer.map(_.render).getOrElse("")
}
