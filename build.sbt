import scala.sys.process.Process

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.6"
libraryDependencies += "org.rogach" %% "scallop" % "3.1.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

val warnings = Seq("dead-code", "inaccessible", "unused", "unused-import")

val commonScalacOptions: Seq[String] = (
  Seq("-unchecked", "-deprecation", "-feature", "-Xlint:_")
    ++ warnings.map("-Ywarn-" + _)
    ++ Seq("-Xmax-classfile-name", "128", "-Xfatal-warnings")
)

lazy val allProjects: Seq[ProjectReference] = Seq(
  common,
  server,
  client
)

lazy val root = Project("coed-root", file("."))
  .aggregate(allProjects: _*)
  .dependsOn(server)
  .dependsOn(client)
  .settings(
    mainClass in (Compile, run) := Some("org.catdog.server.ServerApp"),
    fork in (Compile, run) := true)
    // connectInput in (Compile, run):= true)

lazy val common = Project("coed-common", file("common"))
  .settings(scalacOptions := commonScalacOptions ++ Option(System.getenv().get("SCALA_OPTS")).map(_.split(" ")).toSeq.flatten)

lazy val server = Project("coed-server", file("server"))
  .enablePlugins(StartScripts)
  .settings(
    scalacOptions := commonScalacOptions ++ Option(System.getenv().get("SCALA_OPTS")).map(_.split(" ")).toSeq.flatten,
    testOptions in Test ++= Seq(Tests.Argument(TestFrameworks.ScalaTest, "-y", "org.scalatest.FreeSpec")))
  .dependsOn(common % "test->test;compile->compile")



lazy val client = Project("coed-client", file("client"))
  .enablePlugins(StartScripts)
  .settings(
    scalacOptions := commonScalacOptions ++ Option(System.getenv().get("SCALA_OPTS")).map(_.split(" ")).toSeq.flatten,
    testOptions in Test ++= Seq(Tests.Argument(TestFrameworks.ScalaTest, "-y", "org.scalatest.FreeSpec")))
  .dependsOn(common % "test->test;compile->compile")
