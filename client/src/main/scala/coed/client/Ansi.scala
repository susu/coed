package coed.client

object Ansi {
  val clearScreenCode: String = moveCursorCode(0, 0) ++ "\u001B[2J"
  def moveCursorCode(x: Int, y: Int): String = s"\u001B[${y};${x}H"

  def colorForeground(fg: Int): String = s"\u001B[38;5;${fg}m"
  def colorBackground(bg: Int): String = s"\u001B[48;5;${bg}m"

  val resetColor: String = "\u001B[0m"
  val bold: String = "\u001B[1m"
  val resetBold: String = "\u001B[22m"

  val resetStyle: String = resetBold + resetColor
}
