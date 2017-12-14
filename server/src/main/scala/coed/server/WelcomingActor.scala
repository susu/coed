package coed.server

import akka.actor.{Actor, ActorRef, Props, Terminated}
import coed.common.Protocol.{Edit, Join, Sync}

import scala.collection.mutable

class WelcomingActor extends Actor {

  val clients: mutable.HashSet[ActorRef] = new mutable.HashSet[ActorRef]()

  val serverActor = context.actorOf(Props(new BufferActor("asd")))

  override def receive = {
    case Join =>
      val newClient = sender()
      clients.add(newClient)
      context.watch(newClient)
      serverActor forward Join

    case edit: Edit =>
      serverActor forward edit

    case Sync(c, rev) =>
      clients.foreach(_ ! Sync(c, rev))

    case Terminated(client) =>
      println(s"Client left $client")
      clients.remove(client)
  }
}
