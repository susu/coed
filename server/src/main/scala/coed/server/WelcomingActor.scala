package coed.server

import akka.actor.{Actor, ActorRef, Props}
import akka.event.Logging
import coed.common.Protocol._
import coed.server.InternalMessage.PersistBuffer
import coed.server.persistence.Workspace

import scala.collection.mutable


class WelcomingActor extends Actor {
  type BufferActorRef = ActorRef
  type ClientActorRef = ActorRef

  import WelcomingActor.{ClientDisconnected, ClientInfo, BufferInfo}

  val log = Logging(context.system, this)
  val workspace: String = context.system.settings.config.getString("coed.workspace")

  var connectedUsers: List[ClientInfo] = Nil

  val buffers: mutable.Map[BufferId, BufferInfo] = new mutable.HashMap()

  override def receive: Receive = {
    case Join(user) =>
      log.info(s"User joined: $user (${sender().path})")
      context.watchWith(sender(), ClientDisconnected(user, sender()))
      connectedUsers = ClientInfo(user, sender()) :: connectedUsers
      sender() ! JoinSuccess(Workspace.listBuffers(workspace).toList)

    case o: Open =>
      handleOpenMessage(o)

    case edit: Edit =>
      handleEditMessage(edit)

    case s: Sync =>
      handleSyncMessage(s)

    case Persist(bid) =>
      buffers(bid).buffer ! PersistBuffer

    case ClientDisconnected(user, client: ClientActorRef) =>
      log.info(s"User left: $user, ${client.path}")
      connectedUsers = connectedUsers.filterNot(_.actorRef == client)

      buffers.foreach { case (bufferid, BufferInfo(bufferActor, clientSet)) =>
        clientSet.find(_.actorRef == client).map(clientSet -= _)

        if (clientSet.isEmpty)
        {
          bufferActor ! PersistBuffer
        } else {
          val userList = clientSet.map(_.user).toList
          clientSet.foreach { _.actorRef ! SyncUserList(bufferid, userList) }
        }
      }
  }

  private def handleOpenMessage(openMsg: Open): Unit = {
    val clientInfo = connectedUsers.find(_.actorRef == sender()).get

    val bid = openMsg.bufferId
    log.info(s"User '${clientInfo.user}' opening buffer $bid")
    if (buffers.contains(bid)) {
      buffers(bid).buffer forward openMsg
      buffers(bid).clients.add(clientInfo)
    } else {
      val bufferActor = context.actorOf(Props(new BufferActor(bid, workspace)))
      val valueToInsert = BufferInfo(bufferActor, mutable.Set(clientInfo))
      buffers += (bid -> valueToInsert)
      bufferActor forward openMsg
    }
    val userList = buffers(bid).clients.map(_.user).toList
    buffers(bid).clients.foreach(_.actorRef ! SyncUserList(bid, userList))
  }

  private def handleEditMessage(editMsg: Edit): Unit = {
    val bid = editMsg.bufferId
    buffers(bid).buffer forward editMsg
  }

  private def handleSyncMessage(syncMsg: Sync): Unit= {
    val bid = syncMsg.bufferId
    buffers(bid).sendToClients(syncMsg, self)
  }
}

object WelcomingActor {
  case class ClientDisconnected(user: String, clientActorRef: ActorRef)
  case class ClientInfo(user: String, actorRef: ActorRef)

  case class BufferInfo(buffer: ActorRef, clients: mutable.Set[ClientInfo]) {
    def sendToClients(msg: Any, self: ActorRef): Unit = clients.foreach { _.actorRef.tell(msg, self) }
  }

  case class ClientEditorInfo(info: ClientInfo, cursorPos: Int)
}
