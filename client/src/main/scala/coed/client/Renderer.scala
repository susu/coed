package coed.client

import coed.common.{Buffer, Command, Frame, StringBuf}

trait Renderer {
  def newBuffer(text: String, revision: Long): Unit
  def syncBuffer(command: Command, revision: Long): Unit
  def moveLeft(): Unit
  def moveRight(): Unit
  def moveUp(): Unit
  def moveDown(): Unit
}

class SimpleRenderer extends Renderer {
  private var buffer: Option[Buffer] = None
  private var frame: Frame = Frame("")

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
    buffer = Some(new StringBuf(text))
    frame = Frame(bufferText = buffer.get.render)
    printFrame()
  }

  override def syncBuffer(command: Command, revision: Long): Unit = {
    buffer =
      buffer.map(oldBuffer => oldBuffer.applyCommand(command).getOrElse(oldBuffer))
      frame = Frame(bufferText = buffer.get.render)
      printFrame()
  }

  private def printFrame(): Unit = {
    print(Ansi.clearScreenCode)
    frame.visibleLines.foreach { line => println(line + "\r") }
    print(Ansi.moveCursorCode(frame.cursorPosition.at, frame.cursorPosition.line))
  }
}

