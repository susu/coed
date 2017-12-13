package coed.client

import com.typesafe.config.{Config, ConfigFactory}
import org.rogach.scallop.{ScallopConf, ScallopOption}

object Coed extends App {

  class Arguments(arguments: Seq[String]) extends ScallopConf(arguments) {
    val serverIp: ScallopOption[String] = opt[String](short = 's', descr = "IP of the server", required = true)

    verify()
  }

  println("Welcome to the fantastic Coed editor")

  val arguments = new Arguments(args)

  val localAddresss: IpAddress = LocalIp.whatIsMyIp(arguments.serverIp()).getOrElse {
    println(s"Could not determine local IP from given server IP: ${arguments.serverIp()}")
    sys.exit(42)
  }

  prepareAkkaRemote(localAddresss)

  val cli: Cli = new Cli( c => println(c) )

  def prepareAkkaRemote(localIp: IpAddress) = {
    System.err.println(s"LocalIP: $localIp")
    val remoteConfig: Config = ConfigFactory.parseString(
      s"""akka.remote.netty.tcp.hostname = "${localIp.addr}"
       """)
   remoteConfig.withFallback(ConfigFactory.load)
  }
}
