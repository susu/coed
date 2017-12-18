/*
 * Copyright (c) 2017 BalaBit
 * All rights reserved.
 */

package coed.client

import akka.actor.{Actor, ActorSelection}
import coed.common.Protocol._
import coed.common._

class ClientActor(remoteActor: ActorSelection) extends Actor {
  import ClientActor.cursorPosition

  remoteActor ! Join

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
      frame = frame.copy(bufferText = buffer.render)
      render()

    case Sync(bufferId, cmd, r) =>
      log(s"Sync: $bufferId, $cmd, $r")
      buffer.applyCommand(cmd) match {
        case Right(newBuffer) => {
          frame = syncFrame(buffer, newBuffer, cmd)
          buffer = newBuffer
          render()
        }
        case Left(err) => log(err.toString)
      }

    case move: InternalMessage.MoveCursor =>
      handleMoveCursor(move)
      render()

    case InternalMessage.SaveBuffer =>
      remoteActor ! Persist(currentBufferId.get)

    case InternalMessage.Delete =>
      remoteActor ! Edit(currentBufferId.get, Delete(cursorPosition(frame, buffer), 1), 0)

    case InternalMessage.InsertText(text) =>
      remoteActor ! Edit(currentBufferId.get, Insert(text, cursorPosition(frame, buffer)), 0)

    case InternalMessage.InsertAfterText(text) =>
      remoteActor ! Edit(currentBufferId.get, Insert(text, cursorPosition(frame, buffer) + 1), 0)

    case InternalMessage.TextInsertBufferChanged(text) =>
      BufferRenderer.showAlternateBuffer(text)

    case InternalMessage.CommandBufferChanged(buffer) =>
      BufferRenderer.showAlternateBuffer(buffer)
  }

  private def syncFrame(oldBuffer:Buffer, newBuffer: Buffer, cmd: Command): Frame = {
    def lines(b: Buffer): Int = b.render.lines.size

    val lineDiff: Int = lines(oldBuffer) - lines(newBuffer)
    val newFrame: Frame = if (cmd.position < cursorPosition(frame, oldBuffer)) {
      val (offsetX, offsetY) = frame.bufferOffset
      frame.copy(
        bufferText = newBuffer.render,
        bufferOffset = (offsetX, offsetY - lineDiff))
    } else {
      //todo: handle inside frame changes
      frame.copy(bufferText = newBuffer.render)
    }
    newFrame
  }

  private def render(): Unit = BufferRenderer.show(frame)

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

object ClientActor {
  def cursorPosition(thisFrame: Frame, buf: Buffer): Int = {
    val x: Int = thisFrame.bufferOffset._1 + thisFrame.cursorPosition.at - 1
    val y: Int = thisFrame.bufferOffset._2 + thisFrame.cursorPosition.line - 1
    coordinateToPosition(x, y, buf)
  }

  private def coordinateToPosition(x: Int, y: Int, buf: Buffer): Int =
    buf.render.lines.take(y).toVector.map(_.length + 1).sum + x
}
