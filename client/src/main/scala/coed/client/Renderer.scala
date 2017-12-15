package coed.client

import coed.common.{Buffer, Command, Frame, StringBuf}

trait Renderer {
  def newBuffer(text: String, revision: Long): Unit
  def syncBuffer(command: Command, revision: Long): Unit
  def moveLeft(): Unit
  def moveRight(): Unit
  def moveUp(): Unit
  def moveDown(): Unit

  def moveToLineStart(): Unit

  def cursorPosition: Int
}

class SimpleRenderer extends Renderer {
  private var buffer: Buffer = new StringBuf("")
  private var frame: Frame = Frame("")

  override def cursorPosition: Int = {
    val x: Int = frame.bufferOffset._1 + frame.cursorPosition.at - 1
    val y: Int = (frame.bufferOffset._2 - 1) + frame.cursorPosition.line
    coordinateToPosition(x, y)
  }

  override def moveLeft(): Unit = {
    frame = frame.moveCursorLeft
    printFrame()
  }
  override def moveRight(): Unit = {
    frame = frame.moveCursorRight
    printFrame()
  }
  override def moveUp(): Unit = {
    frame = frame.moveCursorUp
    printFrame()
  }
  override def moveDown(): Unit = {
    frame = frame.moveCursorDown
    printFrame()
  }

  override def moveToLineStart(): Unit = {
    val newPosition = frame.cursorPosition.copy(at = 1)
    frame = frame.copy(cursorPosition = newPosition)
    printFrame()
  }

  override def newBuffer(text: String, revision: Long): Unit = {
    buffer = new StringBuf(text)
    frame = Frame(bufferText = buffer.render)
    printFrame()
  }

  override def syncBuffer(command: Command, revision: Long): Unit = {
    val oldBuffer = buffer
    buffer = buffer.applyCommand(command).getOrElse(buffer)
    val lineDiff: Int = buffer.render.lines.length - oldBuffer.render.lines.length
    val (x, y) = frame.bufferOffset
    frame = Frame(buffer.render, bufferOffset = (x, y+lineDiff), cursorPosition = frame.cursorPosition)
    printFrame()
  }

  private def printFrame(): Unit = {
    print(Ansi.clearScreenCode)
    frame.visibleLines.foreach { line => println(line + "\r") }
    print(Ansi.moveCursorCode(frame.cursorPosition.at, frame.cursorPosition.line))
  }

  def coordinateToPosition(x: Int, y: Int): Int = buffer.render.lines.take(y - 1).toVector.map(_.length + 1).sum+x
  def isBeforeFrame(position: Int): Boolean = position < coordinateToPosition(frame.bufferOffset._1, frame.bufferOffset._2)
}

