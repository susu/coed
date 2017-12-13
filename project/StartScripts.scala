
import java.io.{BufferedWriter, File, FileWriter}
import sbt._
import sbt.Keys._
import scala.sys.process._

object StartScripts extends AutoPlugin {
  lazy val makeKutyus = taskKey[Unit]("blabla")

  override lazy val projectSettings = Seq(
    makeKutyus in Compile := {
      val classpath = (fullClasspath in Compile).value.map(_.data.getAbsolutePath).mkString(":")
      val javaopts = (javaOptions in Compile).value.mkString(" ")

      Seq("mkdir", "-p", "bin").!

      for { cls <- (discoveredMainClasses in Compile).value} {
        val script = scriptContent(cls, javaopts, classpath)
        val name = cls.split('.').last
        writeToFile(script, name)
      }
    }
  )

  def writeToFile(script: String, binaryName: String): Unit = {
    val binaryFile = new File("bin", binaryName)
    val writer = new BufferedWriter(new FileWriter(binaryFile))
    writer.write(script)
    writer.close()
    binaryFile.setExecutable(true)
  }

  private def scriptContent(cls: String, javaopts: String, classpath: String) = s"""#!/bin/bash
                                 |java \\
                                 |    ${javaopts} \\
                                 |    $$JAVA_OPTS \\
                                 |    -classpath $classpath \\
                                 |    $cls "$$@"
                                 |""".stripMargin
}
