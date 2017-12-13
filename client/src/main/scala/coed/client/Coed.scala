package coed.client

import com.typesafe.config.{Config, ConfigFactory}
import org.rogach.scallop.{ScallopConf, ScallopOption}
import akka.actor.{ActorSystem, Props}
import coed.common.IpAddress
import coed.common.Protocol.Edit

object Coed extends App {

  class Arguments(arguments: Seq[String]) extends ScallopConf(arguments) {
    val serverIp: ScallopOption[String] = opt[String](short = 's', descr = "IP of the server", required = true)

    verify()
  }

  println("Welcome to the fantastic Coed editor")

  val arguments = new Arguments(args)

  val localAddresss: IpAddress = IpAddress.whatIsMyIp(arguments.serverIp()).getOrElse {
    println(s"Could not determine local IP from given server IP: ${arguments.serverIp()}")
    sys.exit(42)
  }

  prepareAkkaRemote(localAddresss)

  def prepareAkkaRemote(localIp: IpAddress) = {
    System.err.println(s"LocalIP: $localIp")
    val remoteConfig: Config = ConfigFactory.parseString(
      s"""akka.remote.netty.tcp.hostname = "${localIp.addr}"
       """)
   remoteConfig.withFallback(ConfigFactory.load)
  }

  val actorSystem = ActorSystem("coed")

  val server = actorSystem.actorOf(Props(new ServerActor("asd")))
  val client = actorSystem.actorOf(Props(new ClientActor(server)))

  val cli: Cli = new Cli( c => client ! Edit(c, 0) )
}
