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

  import WelcomingActor.ClientDisconnected

  val log = Logging(context.system, this)
  val workspace: String = context.system.settings.config.getString("coed.workspace")

  val buffers: mutable.Map[BufferId, (BufferActorRef, mutable.Set[ClientActorRef])] = new mutable.HashMap()

  override def receive: Receive = {
    case Join(user) =>
      log.info(s"User joined: $user")
      context.watchWith(sender(), ClientDisconnected(user, sender()))
      sender() ! JoinSuccess(Workspace.listBuffers(workspace).toList)

    case o: Open =>
      handleOpenMessage(o)

    case edit: Edit =>
      handleEditMessage(edit)

    case s: Sync =>
      handleSyncMessage(s)

    case Persist(bid) =>
      buffers(bid)._1 ! PersistBuffer

    case ClientDisconnected(user, client: ClientActorRef) =>
      log.info(s"User left: $user")
      buffers.values.foreach { case (bufferActor, clientSet) =>
        clientSet.remove(client)

        if (clientSet.isEmpty)
        {
          bufferActor ! PersistBuffer
        }
      }
  }

  private def handleOpenMessage(openMsg: Open): Unit = {
    val bid = openMsg.bufferId
    if (buffers.contains(bid)) {
       buffers(bid)._1 forward openMsg
       buffers(bid)._2.add(sender())
    } else {
      val bufferActor = context.actorOf(Props(new BufferActor(bid, workspace)))
      val valueToInsert = (bufferActor, mutable.Set(sender()))
      buffers += (bid -> valueToInsert)
      bufferActor forward openMsg
    }
  }

  private def handleEditMessage(editMsg: Edit): Unit = {
    val bid = editMsg.bufferId
    buffers(bid)._1 forward editMsg
  }

  private def handleSyncMessage(syncMsg: Sync): Unit= {
    val bid = syncMsg.bufferId
    buffers(bid)._2.foreach { _ ! syncMsg }
  }
}

object WelcomingActor {
  case class ClientDisconnected(user: String, clientActorRef: ActorRef)
}
