package coed.client

import coed.client.Cli._

import scala.annotation.tailrec

class Cli(send: KeyPress => Unit) {

  val thread = new Thread(() => {

    def readKey: Int = System.in.read

    @tailrec
    def loop(key: Int): Unit = {
      handleKey(key) match {
        case Some(Continue) =>
          loop(readKey)

        case None =>
          println("error")
          loop(readKey)

        case Some(Stop) => System.exit(0)
      }
    }

    loop(' ')
  })
  thread.setDaemon(true)
  thread.start()

  def handleKey(key: Int): Option[Action] = {
    if (32 <= key && key <= 126) {
      send(Character(key.toChar))
    } else if (key == 27) {
      send(Escape)
    } else if (key == 10 || key == 13) {
      send(Enter)
    } else {
      Console.err.println(s"Unknown input: $key")
    }
    Some(Continue)
  }
}

object Cli {
  sealed trait Action
  case object Continue extends Action
  case object Stop extends Action
}
