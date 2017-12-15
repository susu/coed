package coed.server

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import akka.actor.Actor
import coed.common.Protocol._
import coed.common.{Buffer, StringBuf}
import coed.server.InternalMessage.PersistBuffer

import scala.io.Source
import scala.util.{Failure, Success, Try}

class BufferActor(filename: String) extends Actor {

  var buffer: Buffer = new StringBuf(loadBuffer)

  private def loadBuffer: String = {
    val fileHandle = Try { Source.fromFile(filename) }
    val returnValue = fileHandle.map(_.mkString).getOrElse("")

    fileHandle match {
      case Success(fh) => fh.close()
      case Failure(_) => Unit
    }

    returnValue
  }

  override def receive = {
    case Open(_) =>
      sender() ! OpenSuccess(buffer.render, 0)

    case Edit(bid, c, _) =>
      buffer = buffer.applyCommand(c).getOrElse(buffer)
      context.parent ! Sync(bid, c, 0)

    case PersistBuffer =>
      Files.write(Paths.get(filename), buffer.render.getBytes(StandardCharsets.UTF_8))
  }
}
