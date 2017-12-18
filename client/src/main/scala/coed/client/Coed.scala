package coed.client

import akka.actor.{ActorSelection, ActorSystem, Props}
import coed.common.{AkkaConfigFactory, IpAddress}
import org.rogach.scallop.{ScallopConf, ScallopOption}

object Coed extends App {

  class Arguments(arguments: Seq[String]) extends ScallopConf(arguments) {
    val serverIp: ScallopOption[String] = opt[String](short = 's', descr = "IP of the server", required = true)
    val localIp: ScallopOption[String] = opt[String](short = 'l', descr = "visible IP of the client", required = false)

    verify()
  }

  println("Welcome to the fantastic Coed editor")

  val arguments = new Arguments(args)

  val localAddress: IpAddress = arguments.localIp.toOption.flatMap(IpAddress.validateIp(_)).getOrElse {
    IpAddress.whatIsMyIp(arguments.serverIp()).getOrElse {
      println(s"Could not determine local IP from given server IP: ${arguments.serverIp()}")
      sys.exit(42)
    }
  }

  val actorSystem = ActorSystem("coed", AkkaConfigFactory.remoteConfig(localAddress))

  val actorPath = AkkaConfigFactory.getWelcomeActorPath(arguments.serverIp(), 42000)
  val welcomeActor: ActorSelection = actorSystem.actorSelection(actorPath)

  val client = actorSystem.actorOf(Props(new ClientActor(welcomeActor)))

  val keypressHandler = actorSystem.actorOf(Props(new KeypressHandlerActor(client)))

  val cli: Cli = new Cli( keypress => keypressHandler ! KeyPressMessage(keypress))
}
