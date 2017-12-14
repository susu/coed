package coed.client

import coed.common.{Buffer, StringBuf, Command}

trait BufferUpdater {
  def newBuffer(text: String, revision: Long): Unit
  def syncBuffer(command: Command, revision: Long): Unit
}

class SimpleBufferUpdater extends BufferUpdater {
  private var buffer: Option[Buffer] = None

  println(Ansi.clearScreenCode)

  override def newBuffer(text: String, revision: Long): Unit = {
    buffer = Some(new StringBuf(text))
    printBuffer
  }

  override def syncBuffer(command: Command, revision: Long): Unit = {
    buffer =
      buffer.map(oldBuffer => oldBuffer.applyCommand(command).getOrElse(oldBuffer))
      printBuffer
  }

  private def printBuffer(): Unit = {
    println(Ansi.clearScreenCode ++ renderBuffer)
    println("-----------------------------")
  }
  private def renderBuffer: String = buffer.map(_.render).getOrElse("")
}
