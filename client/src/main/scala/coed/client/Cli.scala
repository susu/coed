package coed.client

import jline.console.ConsoleReader
import scala.annotation.tailrec
import coed.common.{Insert, Delete, Command}
import scala.util.Try

class Cli(send: Command => Unit) {
  val reader = new ConsoleReader()
  reader.setPrompt("kutyus > ")
  new Thread(() => {

    def readLine = Option(reader.readLine)

    @tailrec
    def loop(line: Option[String]): Unit =
      line.flatMap(handleCommand) match {

        case Some(Continue) =>
          loop(readLine)

        case None =>
          println(helpText)
          loop(readLine)

        case _ =>
      }

    loop(Some(""))
  }).start()

  def handleCommand(line: String): Option[Action] = Try {
    val separator: Char = ' '
    val args: List[String] = line.split(separator).toList
    args match {
      case "i"::pos::text => {
        send(Insert(text.mkString(separator.toString), Integer.parseInt(pos)))
        Some(Continue)
      }
      case "d"::pos::length::Nil => {
        send(Delete(Integer.parseInt(pos), Integer.parseInt(length)))
        Some(Continue)
      }
      case "q"::_ => Some(Stop)
      case _ => None
    }
  }.getOrElse(None)

  sealed trait Action
  case object Continue extends Action
  case object Stop extends Action

  val helpText: String = """
    insert text at position: i <pos> <text>
    delete text at position: d <pos> <length>
    quit (you don't want to): q
  """
}
