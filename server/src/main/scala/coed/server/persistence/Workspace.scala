package coed.server.persistence

import java.io.File

object Workspace {
  def listBuffers(workspace: String): Array[String] = {
    val workspaceHandle = new File(workspace)
    if (workspaceHandle.exists() && workspaceHandle.isDirectory) {
      workspaceHandle.listFiles().map(f => f.getName) ++ Array("buffer" + workspaceHandle.listFiles().length)
    }
    else {
      Array("buffer0")
    }
  }
}
