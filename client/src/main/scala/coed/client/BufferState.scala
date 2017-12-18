package coed.client

import coed.common.{Buffer, Command, Frame, StringBuf}

class BufferState extends {
  private var buffer: Buffer = new StringBuf("")

  private var frame: Frame = Frame("")

  def newBuffer(text: String, revision: Long): Unit = {
    buffer = new StringBuf(text)
    print(revision) // to avoid warning
    frame = Frame(bufferText = buffer.render)
  }

  def syncBuffer(command: Command, revision: Long): Unit = {
    val oldBuffer = buffer
    print(revision) // to avoid warning
    buffer = buffer.applyCommand(command).getOrElse(buffer)
    val lineDiff: Int = buffer.render.lines.length - oldBuffer.render.lines.length
    val (x, y) = frame.bufferOffset
    frame = Frame(buffer.render, bufferOffset = (x, y+lineDiff), cursorPosition = frame.cursorPosition)
  }

  def cursorPosition: Int = {
    val x: Int = frame.bufferOffset._1 + frame.cursorPosition.at - 1
    val y: Int = (frame.bufferOffset._2 - 1) + frame.cursorPosition.line
    coordinateToPosition(x, y)
  }

  def moveLeft(): Unit = {
    frame = frame.moveCursorLeft
  }
  def moveRight(): Unit = {
    frame = frame.moveCursorRight
  }
  def moveUp(): Unit = {
    frame = frame.moveCursorUp
  }
  def moveDown(): Unit = {
    frame = frame.moveCursorDown
  }

  private def coordinateToPosition(x: Int, y: Int): Int = buffer.render.lines.take(y - 1).toVector.map(_.length + 1).sum+x
}