package coed.client

import scala.annotation.tailrec
import coed.common.{Insert, Delete, Command}
import scala.util.Try

case class Cursor(val x: Int, val y: Int) {
  def up: Cursor = this.copy(y=Math.max(y-1, 0))
  def down: Cursor = this.copy(y=Math.min(y+1, 25))
  def left: Cursor = this.copy(x=Math.max(x-1, 0))
  def right: Cursor = this.copy(x=Math.min(x+1, 80))
}

class Cli(send: Command => Unit) {
  var cursor: Cursor = Cursor(0, 0)

  new Thread(() => {

    def readChar: Char = System.in.read.toChar

    @tailrec
    def loop(key: Char): Unit = {
      handleKey(key) match {
        case Some(Continue) =>
          loop(readChar)

        case None =>
          println("error")
          loop(readChar)

        case Some(Stop) => System.exit(0)
      }
    }
    loop(' ')
  }).start()

  def handleKey(key: Char): Option[Action] = Try {
    key match {
      case 'q' => Some(Stop)
      case 'i' => {
        send(Insert("kutyus", cursor.x-1))
        Some(Continue)
      }
      case 'x' => {
        send(Delete(cursor.x-1, 1))
        Some(Continue)
      }
      case 'h' =>
        cursor = cursor.left
        print(Ansi.moveCursorCode(cursor.x, cursor.y))
        Some(Continue)
      case 'j' =>
        cursor = cursor.down
        print(Ansi.moveCursorCode(cursor.x, cursor.y))
        Some(Continue)
      case 'k' =>
        cursor = cursor.up
        print(Ansi.moveCursorCode(cursor.x, cursor.y))
        Some(Continue)
      case 'l' =>
        cursor = cursor.right
        print(Ansi.moveCursorCode(cursor.x, cursor.y))
        Some(Continue)
      case _ => None
    }
  }.getOrElse(None)

  sealed trait Action
  case object Continue extends Action
  case object Stop extends Action
}
