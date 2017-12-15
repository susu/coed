package coed.server.persistence

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import coed.common.Protocol.BufferId

import scala.io.Source
import scala.util.{Failure, Success, Try}

class BufferFile(val workspace: String, val bufferId: BufferId)
{
  val filename: String = workspace + bufferId

  def load: String = {
    val fileHandle = Try { Source.fromFile(filename) }
    val returnValue = fileHandle.map(_.mkString).getOrElse("")

    fileHandle match {
      case Success(fh) => fh.close()
      case Failure(_) => Unit
    }

    returnValue
  }

  def persist(text: String): Unit = {
    if (!workspaceExists) createWorkspace
    persistBuffer(text)
  }

  private def workspaceExists = {
    Files.exists(Paths.get(workspace))
  }

  private def createWorkspace = {
    new File(workspace).mkdirs()
  }

  private def persistBuffer(text: String) = {
    Files.write(Paths.get(filename), text.getBytes(StandardCharsets.UTF_8))
  }
}
