package coed.client

object Coed extends App {
  println("Welcome to the fantastic Coed editor")
  val cli: Cli = new Cli( c => println(c) )
}
