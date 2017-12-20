package coed.common

import scala.annotation.tailrec

class StringBuf(text: String) extends Buffer {
  override def applyCommand(command: Command): Either[BufferError, Buffer] = command match {
    case Insert(newText, Buffer.Position(position)) => {
      val (left, right) = text.splitAt(position)
      Right(new StringBuf(left ++ newText ++ right))
    }
    case Delete(Buffer.Position(position), length) => {
      val (left, right) = text.splitAt(position)
      Right(new StringBuf(left ++ right.drop(length)))
    }
  }

  override def render(start: Buffer.LineIndex, end: Buffer.LineIndex): Vector[Buffer.Line] = StringBuf.range(text, start, end)
  override def renderAll: String = text
  override val start: Buffer.Position = Buffer.Position(0)
  override val end: Buffer.Position = Buffer.Position(text.length)
}

object StringBuf {
  private def findNth(n: Int, c: Char, str: String): Int = {
    @tailrec
    def inner(i: Int, from: Int): Int = {
      if (i == n) from - 1
      else {
        val nextIndex: Int = str.indexOf(c, from)
        inner(i + 1, nextIndex + 1)
      }
    }

    inner(0, 0)
  }

  private def range(str: String, start: Buffer.LineIndex, end: Buffer.LineIndex): Vector[Buffer.Line] = {
    val s: Int = findNth(start.index, '\n', str)
    val e: Int = findNth(end.index, '\n', str)

    println("r1")
    if (s > 0 && e > 0 && e > s) {
      println(s"r2|$s|$e|")
      val lines = str.substring(s, e).lines
      val poses = lines.scanLeft(s)( (acc, line) => acc + line.length)
      lines.zip(poses).zip((s to e).toIterator).map( {
        case ((line, pos), n) => Buffer.Line(line, Buffer.LineIndex(n), Buffer.Position(pos))
      } ).toVector
    }
    else Vector()
  }

}

