package coed.server

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import coed.common.Protocol._
import coed.server.persistence.Workspace
import org.scalatest.{FreeSpecLike, Matchers}

class WelcomingActorSuite extends TestKit(ActorSystem("test")) with FreeSpecLike with Matchers {
  trait Fixture {
    val welcomingActor = TestActorRef(Props{ new WelcomingActor(new FakeWorkspace) })
    val client = TestProbe("client")

    def joinClient(): Unit = {
      client.send(welcomingActor, Join("cica"))
      client.expectMsgType[JoinSuccess]
    }
  }

  "upon Join it response with list of buffers" in new Fixture {
    client.send(welcomingActor, Join("cica"))
    val joinSuccess = client.expectMsgType[JoinSuccess]
    joinSuccess.bufferList shouldEqual List("alma")
  }

  "joined client receives OpenSuccess for Open" in new Fixture {
    joinClient()
    client.send(welcomingActor, Open("someBuffer"))
    val openSuccess = client.expectMsgType[OpenSuccess]
    openSuccess.buffer shouldEqual "hali"
  }
}

class FakeWorkspace extends Workspace {
  private var content: String = "hali"

  override def listBuffers: List[String] = List("alma")
  override def persist(bufferId: BufferId, text: String): Unit = { content = text }
  override def load(bufferId: BufferId): String = content
}
