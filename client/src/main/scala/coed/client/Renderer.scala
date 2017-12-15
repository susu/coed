package coed.client

import coed.common.{Buffer, Command, Frame, StringBuf}

trait Renderer {
  def newBuffer(text: String, revision: Long): Unit
  def syncBuffer(command: Command, revision: Long): Unit
  def moveLeft(): Unit
  def moveRight(): Unit
  def moveUp(): Unit
  def moveDown(): Unit

  def cursorPosition: Int
}

class SimpleRenderer extends Renderer {
  private var buffer: Buffer = new StringBuf("")
  private var frame: Frame = Frame("")

  override def cursorPosition: Int = {
    val x: Int = frame.bufferOffset._1 + frame.cursorPosition.at
    val y: Int = frame.bufferOffset._2 + frame.cursorPosition.line
    buffer.render.lines.take(y - 1).toVector.map(_.length + 1).sum+x
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

  override def newBuffer(text: String, revision: Long): Unit = {
    buffer = new StringBuf(text)
    frame = Frame(bufferText = buffer.render)
    printFrame()
  }

  override def syncBuffer(command: Command, revision: Long): Unit = {
    buffer = buffer.applyCommand(command).getOrElse(buffer)
    frame = Frame(bufferText = buffer.render)
    printFrame()
  }

  private def printFrame(): Unit = {
    print(Ansi.clearScreenCode)
    frame.visibleLines.foreach { line => println(line + "\r") }
    print(Ansi.moveCursorCode(frame.cursorPosition.at, frame.cursorPosition.line))
  }
}

