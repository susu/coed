/*
 * Copyright (c) 2017 BalaBit
 * All rights reserved.
 */

package coed.common

import akka.actor.ActorPath
import com.typesafe.config.{Config, ConfigFactory}

object AkkaConfigFactory {
  def remoteConfig(localIp: IpAddress): Config = {
    System.err.println(s"LocalIP: $localIp")
    val remoteConfig: Config = ConfigFactory.parseString(
      s"""akka.remote.netty.tcp.hostname = "${localIp.addr}"
       """)

    remoteConfig.withFallback(ConfigFactory.load)
  }

  def remoteConfigWithPort(localIp: IpAddress, port: Int): Config = {
    ConfigFactory.parseString(
      s"""
         |akka.remote.netty.tcp.port = $port
       """.stripMargin
    ).withFallback(remoteConfig(localIp))
  }

  val WelcomingActorName: String = "welcome"
  val ServerActorSystemName: String = "server"

  def getWelcomeActorPath(ip: String, port: Int): ActorPath =
    ActorPath.fromString(s"akka.tcp://$ServerActorSystemName@$ip:$port/user/$WelcomingActorName")
}
