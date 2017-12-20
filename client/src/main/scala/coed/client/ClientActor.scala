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

  lazy val currentUserName: String = Option(System.getenv("USER")).getOrElse("rainbow-unicorn-42")

  remoteActor ! Join(currentUserName)

  private val log = Logging(context.system, this)

  private var buffer: Buffer = new StringBuf("")
  private var frame: Frame = Frame(buffer, log = log)

  private var currentBufferId: Option[BufferId] = None
  private var currentUserList: List[String] = Nil

  override def receive: Receive = {
    case JoinSuccess(bufferList) =>
      currentBufferId = Some(bufferList.head)
      remoteActor ! Open(bufferList.head)

    case OpenSuccess(bufferContent, revision) =>
      log.info(s"Buffer opened: $currentBufferId, $revision")
      buffer = new StringBuf(bufferContent)
      frame = frame.copy(buffer)
      render()

    case Sync(bufferId, cmd, r) =>
      log.info(s"Sync: $bufferId, $cmd, $r")
      buffer.applyCommand(cmd) match {
        case Right(newBuffer) => {
          frame = frame.copy(buffer = newBuffer) //todo: sync cursor position
          buffer = newBuffer
          render()
        }
        case Left(err) => log.info(err.toString)
      }

    case SyncUserList(_, users) =>
      log.debug(s"SyncUserList: $users")
      currentUserList = users
      render()

    case move: InternalMessage.MoveCursor =>
      handleMoveCursor(move)
      render()

    case InternalMessage.SaveBuffer =>
      remoteActor ! Persist(currentBufferId.get)

    case InternalMessage.Delete =>
      remoteActor ! Edit(currentBufferId.get, Delete(frame.cursorInBuffer, 1), 0)

    case InternalMessage.DeleteLine =>
      remoteActor ! Edit(currentBufferId.get, Delete(frame.currentLine.start, frame.currentLineLength + 1), 0)

    case InternalMessage.DeleteUntilEndOfLine =>
      val restOfLine: Int = frame.currentLineLength - frame.cursorPosition.x
      remoteActor ! Edit(currentBufferId.get, Delete(frame.cursorInBuffer, restOfLine + 1), 0)

    case InternalMessage.InsertText(text) =>
      remoteActor ! Edit(currentBufferId.get, Insert(text, frame.cursorInBuffer), 0)

    case InternalMessage.InsertAfterText(text) =>
      remoteActor ! Edit(currentBufferId.get, Insert(text, frame.cursorInBuffer), 0)

    case InternalMessage.InsertNextLineText(text) =>
      remoteActor ! Edit(currentBufferId.get, Insert("\n" ++ text, frame.cursorInBuffer), 0)

    case InternalMessage.TextInsertBufferChanged(text) =>
      BufferRenderer.showAlternateBuffer(text)

    case InternalMessage.CommandBufferChanged(buffer) =>
      BufferRenderer.showAlternateBuffer(buffer)
  }

  private def render(): Unit = {
    BufferRenderer.show(frame)
    BufferRenderer.showUserList(currentUserList)
  }

  private def handleMoveCursor(cursor: InternalMessage.MoveCursor): Unit = cursor match {
    case InternalMessage.Left => frame = frame.moveCursorLeft
    case InternalMessage.Right => frame = frame.moveCursorRight
    case InternalMessage.Up => frame = frame.moveCursorUp
    case InternalMessage.Down => frame = frame.moveCursorDown
    case InternalMessage.Top => frame = frame.copy(bufferOffset = Buffer.LineIndex(0), cursorPosition = FrameCoord(0, 0))
    case InternalMessage.Bottom => frame = frame.copy(bufferOffset = Buffer.LineIndex(buffer.renderAll.lines.size), cursorPosition = FrameCoord(0, 0))
    case InternalMessage.LineStart => frame = frame.copy(cursorPosition = frame.cursorPosition.copy(x=0))
    case InternalMessage.LineEnd => frame = frame.copy(cursorPosition = frame.cursorPosition.copy(x=frame.currentLineLength - 1))
  }
}

