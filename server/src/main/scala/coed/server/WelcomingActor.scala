package coed.server

import akka.actor.{Actor, ActorRef, Props}
import coed.common.Protocol.{Edit, Join, Sync}

import scala.collection.mutable

class WelcomingActor extends Actor {

  val clients: mutable.HashSet[ActorRef] = new mutable.HashSet[ActorRef]()

  val serverActor = context.actorOf(Props(new ServerActor("asd")))

  override def receive = {
    case Join =>
      clients.add(sender())
      serverActor forward Join
    case edit: Edit =>
      serverActor forward edit
    case Sync(c, rev) =>
      clients.foreach(_ ! Sync(c, rev))
  }
}
