package coed.server

import akka.actor.{Actor, ActorRef, Props, Terminated}
import coed.common.Protocol._

import scala.collection.mutable

class WelcomingActor extends Actor {
  type BufferActorRef = ActorRef
  type ClientActorRef = ActorRef

  val buffers: mutable.Map[BufferId, (BufferActorRef, mutable.Set[ClientActorRef])] = new mutable.HashMap()

  override def receive = {
    case Join =>
      context.watch(sender())
      //sender() ! JoinSuccess(buffers.keys.toList)
      sender() ! JoinSuccess(List("buffer1"))

    case o: Open =>
      handleOpenMessage(o)

    case edit: Edit =>
      handleEditMessage(edit)

    case s: Sync =>
      handleSyncMessage(s)

    case Terminated(client) =>
      buffers.values.foreach { case (_, clientSet) =>
        clientSet.remove(client)
      }
  }

  private def handleOpenMessage(openMsg: Open): Unit = {
    val bid = openMsg.bufferId
    if (buffers.contains(bid)) {
       buffers(bid)._1 forward openMsg
       buffers(bid)._2.add(sender())
    } else {
      val bufferActor = context.actorOf(Props(new BufferActor(bid)))
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
