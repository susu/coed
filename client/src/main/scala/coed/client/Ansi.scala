package coed.client

object Ansi {
  val clearScreenCode: String = moveCursorCode(0, 0) ++ "\u001B[2J"
  def moveCursorCode(x: Int, y: Int): String = s"\u001B[${y};${x}H"
}
