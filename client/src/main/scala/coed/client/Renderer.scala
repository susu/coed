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
  private var frame: Option[Frame] = None

  override def moveLeft(): Unit = frame.foreach(_.moveCursorLeft)
  override def moveRight(): Unit = frame.foreach(_.moveCursorRight)
  override def moveUp(): Unit = frame.foreach(_.moveCursorUp)
  override def moveDown(): Unit = frame.foreach(_.moveCursorDown)

  override def newBuffer(text: String, revision: Long): Unit = {
    buffer = Some(new StringBuf(text))
    frame = Some(Frame(bufferText = buffer.get.render))
    printFrame()
  }

  override def syncBuffer(command: Command, revision: Long): Unit = {
    buffer =
      buffer.map(oldBuffer => oldBuffer.applyCommand(command).getOrElse(oldBuffer))
      frame = Some(Frame(bufferText = buffer.get.render))
      printFrame()
  }

  private def printFrame(): Unit = {
    print(Ansi.clearScreenCode)
    frame.foreach(frame => frame.visibleLines.foreach { line => println(line + "\r") })
  }
}

