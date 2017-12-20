package coed.server

import akka.actor.{ActorSystem, Props}
import coed.common.{AkkaConfigFactory, IpAddress}
import coed.server.persistence.RealWorkSpace
import org.rogach.scallop.{ScallopConf, ScallopOption}

object Server extends App {
  class Arguments(arguments: Seq[String]) extends ScallopConf(arguments) {
    val localIp: ScallopOption[String] = opt[String](short = 'l', descr = "local IP where clients can access this server",
      required = true)
    verify()
  }

  val arguments = new Arguments(args)

  val localIp = IpAddress.validateIp(arguments.localIp()).getOrElse {
    println(s"Invalid local IP: ${arguments.localIp()}")
    sys.exit(42)
  }

  val config = AkkaConfigFactory.remoteConfigWithPort(localIp, 42000)
  val system = ActorSystem(AkkaConfigFactory.ServerActorSystemName, config)

  val workspace = new RealWorkSpace(config.getString("coed.workspace"))

  val welcomingActor = system.actorOf(Props(new WelcomingActor(workspace)),
    AkkaConfigFactory.WelcomingActorName)
}
