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
  .dependsOn(common)

lazy val common = Project("coed-common", file("common"))
  .settings(scalacOptions := commonScalacOptions ++ Option(System.getenv().get("SCALA_OPTS")).map(_.split(" ")).toSeq.flatten)

lazy val server = Project("coed-server", file("server"))
  .settings(
    scalacOptions := commonScalacOptions ++ Option(System.getenv().get("SCALA_OPTS")).map(_.split(" ")).toSeq.flatten,
    testOptions in Test ++= Seq(Tests.Argument(TestFrameworks.ScalaTest, "-y", "org.scalatest.FreeSpec")))


lazy val client = Project("coed-client", file("client"))
  .settings(
    scalacOptions := commonScalacOptions ++ Option(System.getenv().get("SCALA_OPTS")).map(_.split(" ")).toSeq.flatten,
    testOptions in Test ++= Seq(Tests.Argument(TestFrameworks.ScalaTest, "-y", "org.scalatest.FreeSpec")))
