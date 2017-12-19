/*
 * Copyright (c) 2017 BalaBit
 * All rights reserved.
 */

package coed.client

import akka.actor.{Actor, ActorSelection}
import akka.event.Logging
import coed.common.Protocol._
import coed.common._

class ClientActor(remoteActor: ActorSelection) extends Actor {
  import ClientActor.{cursorPosition, framePosition, lineStartPosition}

  lazy val currentUserName: String = Option(System.getenv("USER")).getOrElse("rainbow-unicorn-42")

  remoteActor ! Join(currentUserName)

  private val log = Logging(context.system, this)

  private var buffer: Buffer = new StringBuf("")
  private var frame: Frame = Frame(bufferText = buffer.render, log = log)

  private var currentBufferId: Option[BufferId] = None

  override def receive: Receive = {
    case JoinSuccess(bufferList) =>
      currentBufferId = Some(bufferList.head)
      remoteActor ! Open(bufferList.head)

    case OpenSuccess(bufferContent, revision) =>
      log.info(s"Buffer opened: $currentBufferId, $revision")
      buffer = new StringBuf(bufferContent)
      frame = frame.copy(bufferText = buffer.render)
      render()

    case Sync(bufferId, cmd, r) =>
      log.info(s"Sync: $bufferId, $cmd, $r")
      buffer.applyCommand(cmd) match {
        case Right(newBuffer) => {
          frame = syncFrame(buffer, newBuffer, cmd)
          buffer = newBuffer
          render()
        }
        case Left(err) => log.info(err.toString)
      }

    case move: InternalMessage.MoveCursor =>
      handleMoveCursor(move)
      render()

    case InternalMessage.SaveBuffer =>
      remoteActor ! Persist(currentBufferId.get)

    case InternalMessage.Delete =>
      remoteActor ! Edit(currentBufferId.get, Delete(Buffer.Position(cursorPosition(frame, buffer)), 1), 0)

    case InternalMessage.DeleteLine =>
      remoteActor ! Edit(currentBufferId.get, Delete(Buffer.Position(lineStartPosition(frame, buffer)), frame.currentLineLength + 1), 0)

    case InternalMessage.DeleteUntilEndOfLine =>
      val restOfLine: Int = frame.currentLineLength - frame.cursorPosition.at
      remoteActor ! Edit(currentBufferId.get, Delete(Buffer.Position(cursorPosition(frame, buffer)), restOfLine + 1), 0)

    case InternalMessage.InsertText(text) =>
      remoteActor ! Edit(currentBufferId.get, Insert(text, Buffer.Position(cursorPosition(frame, buffer))), 0)

    case InternalMessage.InsertAfterText(text) =>
      remoteActor ! Edit(currentBufferId.get, Insert(text, Buffer.Position(cursorPosition(frame, buffer) + 1)), 0)

    case InternalMessage.InsertNextLineText(text) =>
      remoteActor ! Edit(currentBufferId.get, Insert("\n" ++ text, Buffer.Position(cursorPosition(frame, buffer) + frame.currentLineLength)), 0)

    case InternalMessage.TextInsertBufferChanged(text) =>
      BufferRenderer.showAlternateBuffer(text)

    case InternalMessage.CommandBufferChanged(buffer) =>
      BufferRenderer.showAlternateBuffer(buffer)
  }

  private def syncFrame(oldBuffer:Buffer, newBuffer: Buffer, cmd: Command): Frame = {
    def lines(b: Buffer): Int = b.render.lines.size

    val lineDiff: Int = lines(oldBuffer) - lines(newBuffer)
    val newFrame: Frame = if (cmd.position < Buffer.Position(framePosition(frame, oldBuffer))) {
      val (offsetX, offsetY) = frame.bufferOffset
      frame.copy(
        bufferText = newBuffer.render,
        bufferOffset = (offsetX, offsetY - lineDiff))
    } else if (cmd.position < Buffer.Position(cursorPosition(frame, oldBuffer))) {
      val oldCoords: FrameCoords = frame.cursorPosition
      frame.copy(
        bufferText = newBuffer.render,
        cursorPosition = FrameCoords(oldCoords.at, oldCoords.line - lineDiff)
      )
    } else {
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
    case InternalMessage.Top => frame = frame.copy(bufferOffset = (0, 0), cursorPosition = FrameCoords(1, 1))
    case InternalMessage.Bottom => frame = frame.copy(bufferOffset = (0, buffer.render.lines.size), cursorPosition = FrameCoords(1, 1))
    case InternalMessage.LineStart => frame = frame.copy(cursorPosition = frame.cursorPosition.copy(at=1))
    case InternalMessage.LineEnd => frame = frame.copy(cursorPosition = frame.cursorPosition.copy(at=frame.currentLineLength))
  }
}

object ClientActor {
  def lineStartPosition(thisFrame: Frame, buf: Buffer): Int = {
    cursorPosition(thisFrame, buf) - thisFrame.cursorPosition.at
  }

  def framePosition(thisFrame: Frame, buf: Buffer): Int = {
    coordinateToPosition(thisFrame.bufferOffset._1, thisFrame.bufferOffset._2, buf)
  }

  def cursorPosition(thisFrame: Frame, buf: Buffer): Int = {
    val x: Int = thisFrame.bufferOffset._1 + thisFrame.cursorPosition.at - 1
    val y: Int = thisFrame.bufferOffset._2 + thisFrame.cursorPosition.line - 1
    coordinateToPosition(x, y, buf)
  }

  private def coordinateToPosition(x: Int, y: Int, buf: Buffer): Int =
    buf.render.lines.take(y).toVector.map(_.length + 1).sum + x
}
