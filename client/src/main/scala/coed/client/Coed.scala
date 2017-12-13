package coed.client

import akka.actor.{ActorSelection, ActorSystem, Props}
import coed.common.Protocol.Edit
import coed.common.{AkkaConfigFactory, IpAddress}
import org.rogach.scallop.{ScallopConf, ScallopOption}

object Coed extends App {

  class Arguments(arguments: Seq[String]) extends ScallopConf(arguments) {
    val serverIp: ScallopOption[String] = opt[String](short = 's', descr = "IP of the server", required = true)

    verify()
  }

  println("Welcome to the fantastic Coed editor")

  val arguments = new Arguments(args)

  val localAddress: IpAddress = IpAddress.whatIsMyIp(arguments.serverIp()).getOrElse {
    println(s"Could not determine local IP from given server IP: ${arguments.serverIp()}")
    sys.exit(42)
  }

  val actorSystem = ActorSystem("coed", AkkaConfigFactory.remoteConfig(localAddress))

  val actorPath = AkkaConfigFactory.getWelcomeActorPath(arguments.serverIp(), 42000)
  val welcomeActor: ActorSelection = actorSystem.actorSelection(actorPath)

  val client = actorSystem.actorOf(Props(new ClientActor(welcomeActor)))

  val cli: Cli = new Cli( c => client ! Edit(c, 0) )
}
