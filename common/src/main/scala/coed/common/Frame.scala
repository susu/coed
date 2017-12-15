package coed.common

case class FrameCoords(line: Int, at: Int) // indexing starts at 1

case class Frame(bufferText: String,
                 bufferOffset: (Int, Int) = (0, 0),  // x and y offsets
                 cursorPosition: FrameCoords = FrameCoords(1, 1), // indexing starts from 1, 1
                 frameWidth: Int = Frame.DEFAULT_FRAME_WIDTH,
                 frameHeight: Int = Frame.DEFAULT_FRAME_HEIGHT) {

  private val linesInBuffer: Vector[String] = bufferText.lines.toVector

  val visibleLines: Seq[String] = (1 to frameHeight).map { calculateVisibleLine(_) }

  def moveCursorUp: Frame = ???
  def moveCursorDown: Frame = ???
  def moveCursorLeft: Frame = ???
  def moveCursorRight: Frame = ???

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


