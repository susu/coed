package coed.client

import coed.common.Frame

object BufferRenderer {
  def show(frame: Frame): Unit = {
    print(Ansi.clearScreenCode)
    frame.visibleLines.foreach { line => println(line + "\r") }
    print(Ansi.moveCursorCode(frame.cursorPosition.at, frame.cursorPosition.line))
  }
}

