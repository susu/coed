/*
 * Copyright (c) 2017 BalaBit
 * All rights reserved.
 */

package coed.client

import akka.actor.{Actor, ActorSelection}
import coed.common.Protocol._
import coed.common._

class NewClientActor(remoteActor: ActorSelection) extends Actor {
  import NewClientActor.cursorPosition

  private var buffer: Buffer = new StringBuf("")
  private var frame: Frame = Frame(bufferText = buffer.render)

  private var currentBufferId: Option[BufferId] = None

  override def receive: Receive = {
    case JoinSuccess(bufferList) =>
      currentBufferId = Some(bufferList.head)
      remoteActor ! Open(bufferList.head)

    case OpenSuccess(bufferContent, revision) =>
      log(s"Buffer opened: $currentBufferId, $revision")
      buffer = new StringBuf(bufferContent)
      syncFrame()

    case Sync(bufferId, cmd, r) =>
      log(s"Sync: $bufferId, $cmd, $r")
      buffer.applyCommand(cmd) match {
        case Right(newBuffer) => {
          buffer = newBuffer
          syncFrame()
        }
        case Left(err) => log(err.toString)
      }

    case move: InternalMessage.MoveCursor => handleMoveCursor(move)

    case InternalMessage.Delete =>
      remoteActor ! Edit(currentBufferId.get, Delete(cursorPosition(frame, buffer), 1), 0)

    case InternalMessage.InsertText(text) =>
      remoteActor ! Edit(currentBufferId.get, Insert(text, cursorPosition(frame, buffer)), 0)
  }

  private def syncFrame(): Unit = {
    // TODO it will not scroll with the updated content
    frame = frame.copy(bufferText = buffer.render)
  }

  private def handleMoveCursor(cursor: InternalMessage.MoveCursor): Unit = cursor match {
    case InternalMessage.Left => frame = frame.moveCursorLeft
    case InternalMessage.Right => frame = frame.moveCursorRight
    case InternalMessage.Up => frame = frame.moveCursorUp
    case InternalMessage.Down => frame = frame.moveCursorDown
  }

  private def log(msg: String): Unit = {
    Console.err.println(msg + "\r\n")
  }
}

object NewClientActor {
  def cursorPosition(thisFrame: Frame, buf: Buffer): Int = {
    val x: Int = thisFrame.bufferOffset._1 + thisFrame.cursorPosition.at - 1
    val y: Int = (thisFrame.bufferOffset._2 - 1) + thisFrame.cursorPosition.line
    coordinateToPosition(x, y, buf)
  }

  private def coordinateToPosition(x: Int, y: Int, buf: Buffer): Int =
    buf.render.lines.take(y - 1).toVector.map(_.length + 1).sum+x
}
