package coed.client

import coed.common.Frame

object BufferRenderer {
  def show(frame: Frame): Unit = {
    print(Ansi.clearScreenCode)
    frame.visibleLines.foreach { line => println(line + "\r") }
    print(Ansi.moveCursorCode(frame.cursorPosition.at, frame.cursorPosition.line))
  }

  def showAlternateBuffer(text: String): Unit = {
    print(Ansi.moveCursorCode(1, Frame.DEFAULT_FRAME_HEIGHT))
    print(text)
  }
}

