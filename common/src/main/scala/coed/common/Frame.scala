package coed.common

import akka.event.LoggingAdapter

case class FrameCoord(x: Int, y: Int) {
  require(x >= 0)
  require(y >= 0)
}

case class Frame(buffer: Buffer,
                 bufferOffset: Buffer.LineIndex = Buffer.LineIndex(0),
                 cursorPosition: FrameCoord = FrameCoord(0, 0),
                 frameWidth: Int = Frame.DEFAULT_FRAME_WIDTH,
                 frameHeight: Int = Frame.DEFAULT_FRAME_HEIGHT,
                 log: LoggingAdapter) {

  require(frameWidth > 0)
  require(frameHeight > 0)
  require(cursorPosition.y < frameHeight)

  println("1")
  val visibleLines: Vector[Buffer.Line] = buffer.render(bufferOffset, bufferOffset.add(frameHeight))

  println("2")
  val currentLine: Buffer.Line = visibleLines(cursorPosition.y)
  println("3")
  val cursorInBuffer: Buffer.Position = currentLine.start.add(cursorPosition.x)
  println("4")
  val startInBuffer: Buffer.Position = visibleLines(0).start
  println("5")

  logDebuginfo()

  def moveCursorUp: Frame = {
    if (cursorPosition.y == 0) {
      this.copy(bufferOffset = this.bufferOffset.moveUp)
    } else {
      this.copy(cursorPosition = FrameCoord(this.cursorPosition.x, this.cursorPosition.y - 1))
    }
  }

  def moveCursorDown: Frame = {
    if (cursorPosition.y == (frameHeight - 1)) {
      if (bufferOffset.index == visibleLines.size - frameHeight) this
      else this.copy(bufferOffset = this.bufferOffset.moveDown)
    } else {
      if (this.cursorPosition.y == visibleLines.size - bufferOffset.index) this
      else this.copy(cursorPosition = FrameCoord(this.cursorPosition.x, this.cursorPosition.y + 1))
    }
  }

  def moveCursorLeft: Frame = {
    if (cursorPosition.x == 0) {
      this
    } else {
      this.copy(cursorPosition = FrameCoord(this.cursorPosition.x - 1, this.cursorPosition.y))
    }
  }

  private def logDebuginfo(): Unit = {
    log.info(s"Frame: bufferoffset=$bufferOffset")
    log.info(s"Frame: cursorpos.line=${cursorPosition.y}")
    log.info(s"Frame: visibleLines.size=${visibleLines.size}")
  }

  val currentLineLength = if (visibleLines.nonEmpty) visibleLines(cursorPosition.y).line.size
                          else 0

  def moveCursorRight: Frame = {
    if (cursorPosition.x == frameWidth) {
      this
    } else {
        if (cursorPosition.x >= currentLineLength) {
          this
        } else  {
          this.copy(cursorPosition = FrameCoord(this.cursorPosition.x + 1, this.cursorPosition.y))
        }
    }
  }
}

object Frame {
  val DEFAULT_FRAME_WIDTH = 80
  val DEFAULT_FRAME_HEIGHT = 25

  val EMPTY_LINE_REPRESENTATION = "~"
}


