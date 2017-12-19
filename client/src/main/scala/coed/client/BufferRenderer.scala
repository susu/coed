package coed.client

import java.io.PrintStream

import coed.common.Frame

class BufferedWriter(output: PrintStream) {
  val buf: StringBuilder = new StringBuilder
  def append(s: String): Unit = buf.append(s)
  def flush(): Unit = output.print(buf)
}

object BufferRenderer {

  def show(frame: Frame): Unit = {
    val output = new BufferedWriter(Console.out)

    output.append(Ansi.clearScreenCode)
    val indexFrom = frame.bufferOffset._2
    val lineNumbers = indexFrom to (indexFrom + Frame.DEFAULT_FRAME_HEIGHT)

    def formatLineNumber(number: Int) =
      s"${Ansi.colorForeground(3)}${leftpad(number)}${Ansi.resetColor}"

    frame.visibleLines.zip(lineNumbers).foreach {
      case (line, number) => output.append(s"${formatLineNumber(number)} ${poorMansSyntaxHighlight(line)}\n\r")
    }
    output.append(Ansi.moveCursorCode(frame.cursorPosition.at + 4, frame.cursorPosition.line))
    output.flush()
  }

  private val Colors: IndexedSeq[String] = (0 to 7).map(Ansi.colorForeground)

  private def poorMansSyntaxHighlight(line: String): String = {
    line.split(' ').map(word => {
      val index = Math.abs(word.hashCode % Colors.length)
      Ansi.bold + Colors(index) + word + Ansi.resetStyle
    }).mkString(" ")
  }

  def showAlternateBuffer(text: String): Unit = {
    val output = new BufferedWriter(Console.out)
    val decoratorLine = Ansi.colorForeground(5) +
      (0 to Frame.DEFAULT_FRAME_WIDTH).map(_ => "-").mkString("") +
      Ansi.resetColor + "\r\n"

    output.append(Ansi.moveCursorCode(1, Frame.DEFAULT_FRAME_HEIGHT + 1))
    output.append(decoratorLine)
    output.append(Ansi.moveCursorCode(1, Frame.DEFAULT_FRAME_HEIGHT + 2))

    // magic split that maintains \n endings
    val lines: Array[String] = text.split("\n", -1)

    lines.foreach(line => {
      output.append(line + Ansi.colorBackground(6))
      output.append("." * (Frame.DEFAULT_FRAME_WIDTH - line.length))
      output.append(Ansi.resetColor + "\n\r")
    })
    output.append(decoratorLine)
    output.flush()
  }

  def leftpad(number: Int): String = {
    f"$number%03d"
  }
}

