package coed.server.persistence

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import coed.common.Protocol.BufferId

import scala.io.Source
import scala.util.Try

trait Workspace {
  def listBuffers: List[String]
  def persist(bufferId: BufferId, text: String): Unit
  def load(bufferId: BufferId): String
}

class RealWorkSpace(path: String) extends Workspace {
  override def listBuffers: List[BufferId] = {
    val workspaceHandle = new File(path)
    val array: Array[BufferId] = if (workspaceHandle.exists() && workspaceHandle.isDirectory) {
      workspaceHandle.listFiles().map(f => f.getName) ++ List("buffer" + workspaceHandle.listFiles().length)
    } else {
      Array("buffer0")
    }

    array.toList
  }

  override def persist(bufferId: BufferId, text: String): Unit = {
    makeDirs()
    Files.write(Paths.get(pathOf(bufferId)), text.getBytes(StandardCharsets.UTF_8))
  }

  override def load(bufferId: BufferId): String = Try {
    Source.fromFile(pathOf(bufferId)).mkString
  }.getOrElse("")

  private def makeDirs(): Unit = {
    new File(path).mkdirs()
  }

  private def pathOf(bufferId: BufferId): String = {
    s"$path/$bufferId"
  }
}
