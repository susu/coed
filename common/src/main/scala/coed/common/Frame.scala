package coed.common

case class FrameCoords(at: Int, line: Int) {
  require(at > 0)
  require(line > 0)
}

/** Class to represent the displayed rectangular section of a buffer.
  *
  * @param bufferText  text of buffer to display in frame
  * @param bufferOffset coordinates of top left corner of frame
  * @param cursorPosition cursor position in frame coordinates
  * @param frameWidth
  * @param frameHeight
  */
case class Frame(bufferText: String,
                 bufferOffset: (Int, Int) = (0, 0),  // x and y offsets
                 cursorPosition: FrameCoords = FrameCoords(1, 1), // indexing starts from 1, 1
                 frameWidth: Int = Frame.DEFAULT_FRAME_WIDTH,
                 frameHeight: Int = Frame.DEFAULT_FRAME_HEIGHT) {

  require(bufferOffset._1 >= 0)
  require(bufferOffset._2 >= 0)
  require(frameWidth > 0)
  require(frameHeight > 0)
  require(cursorPosition.line > 0 && cursorPosition.line <= frameHeight)
  require(cursorPosition.at > 0 && cursorPosition.at <= frameWidth)

  private val linesInBuffer: Vector[String] = bufferText.lines.toVector

  val visibleLines: Seq[String] = (1 to frameHeight).map { calculateVisibleLine(_) }

  def moveCursorUp: Frame = {
    if (cursorPosition.line == 1) {
      if (bufferOffset._2 == 0)  this
      else this.copy(bufferOffset = (this.bufferOffset._1, this.bufferOffset._2 - 1))
    } else {
      this.copy(cursorPosition = FrameCoords(this.cursorPosition.at, this.cursorPosition.line - 1))
    }
  }

  def moveCursorDown: Frame = {
    if (cursorPosition.line == frameHeight) {
      if (bufferOffset._2 == linesInBuffer.size - frameHeight) this
      else this.copy(bufferOffset = (this.bufferOffset._1, this.bufferOffset._2 + 1))
    } else {
      this.copy(cursorPosition = FrameCoords(this.cursorPosition.at, this.cursorPosition.line + 1))
    }
  }

  def moveCursorLeft: Frame = {
    if (cursorPosition.at == 1) {
      if (bufferOffset._1 == 0)  this
      else this.copy(bufferOffset = (this.bufferOffset._1 - 1, this.bufferOffset._2))
    } else {
      this.copy(cursorPosition = FrameCoords(this.cursorPosition.at - 1, this.cursorPosition.line))
    }
  }

  val currentLineLength = if (linesInBuffer.size > 0 ) linesInBuffer(cursorPosition.line - 1).size
                          else 0

  def moveCursorRight: Frame = {
    if (cursorPosition.at == frameWidth) { // cursor is at right edge of frame
        if (bufferOffset._1 == currentLineLength - frameWidth) this
        else this.copy(bufferOffset = (this.bufferOffset._1 + 1, this.bufferOffset._2))
    } else {
        if (bufferOffset._1 + cursorPosition.at >= currentLineLength) { //mid of frame, end of line
          this
        } else  {
          this.copy(cursorPosition = FrameCoords(this.cursorPosition.at + 1, this.cursorPosition.line))
        }
    }
  }

  private def calculateVisibleLine(lineIndexInFrame: Int): String = {
    val fullLineFromBuffer = translateBufferLineToFrameLine(lineIndexInFrame)
    visiblePartFromLine(fullLineFromBuffer)
  }

  private def translateBufferLineToFrameLine(lineIndexInFrame: Int): String = {
    val whichLineInBufferText = lineIndexInFrame + bufferOffset._2  // indexed from 1

    if (whichLineInBufferText > linesInBuffer.size) Frame.EMPTY_LINE_REPRESENTATION
    else linesInBuffer(whichLineInBufferText - 1)
  }

  private def visiblePartFromLine(fullLineFromBuffer: String): String = {
    val strippedFromLeft = fullLineFromBuffer.drop(bufferOffset._1)
    strippedFromLeft.take(frameWidth)
  }
}

object Frame {
  val DEFAULT_FRAME_WIDTH = 80
  val DEFAULT_FRAME_HEIGHT = 25

  val EMPTY_LINE_REPRESENTATION = "~"
}


