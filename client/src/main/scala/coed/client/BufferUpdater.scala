package coed.client

import coed.common.{Buffer, StringBuf, Command}

trait BufferUpdater {
  def newBuffer(text: String, revision: Long): Unit
  def syncBuffer(command: Command, revision: Long): Unit
}

class SimpleBufferUpdater extends BufferUpdater {
  private var buffer: Option[Buffer] = None

  override def newBuffer(text: String, revision: Long): Unit = {
    buffer = Some(new StringBuf(text))
    printBuffer()
  }

  override def syncBuffer(command: Command, revision: Long): Unit = {
    buffer =
      buffer.map(oldBuffer => oldBuffer.applyCommand(command).getOrElse(oldBuffer))
      printBuffer()
  }

  private def printBuffer(): Unit = {
    println(clearScreenCode + renderBuffer)
    println("-----------------------------")
  }
  private def renderBuffer: String = buffer.map(_.render).getOrElse("")

  private val clearScreenCode: String = moveCursorCode(0, 0) ++ "\u001B[2J"
  private def moveCursorCode(x: Int, y: Int): String = s"\u001B[${y};${x}H"
}
