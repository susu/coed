package coed.server

import akka.actor.ActorSystem
import coed.common.{AkkaConfigFactory, IpAddress}
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
  val system = ActorSystem("Server", config)
}