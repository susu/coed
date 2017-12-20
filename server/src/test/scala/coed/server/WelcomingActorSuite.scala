package coed.server

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import coed.common.Buffer.Position
import coed.common.Insert
import coed.common.Protocol._
import coed.server.persistence.Workspace
import org.scalatest.{FreeSpecLike, Matchers}

class WelcomingActorSuite extends TestKit(ActorSystem("test")) with FreeSpecLike with Matchers {
  trait Fixture {
    val bufferId = "alma"
    val welcomingActor = TestActorRef(Props {
      new WelcomingActor(new FakeWorkspace)
    })

    val command = Insert("hello", Position(0))
    val editMsg = Edit(bufferId, command, rev = 0)

    def joinClient(ref: TestProbe, name: String): Unit = {
      ref.send(welcomingActor, Join(name))
      ref.expectMsgType[JoinSuccess]
    }

    def openBuffer(ref: TestProbe, bid: BufferId = bufferId): Unit = {
      ref.send(welcomingActor, Open(bid))
      ref.receiveN(2)
    }
  }

  trait FixtureWithSingleClient extends Fixture {
    val client = TestProbe("client")
    def joinClient(): Unit = joinClient(client, "cica")
    def openBuffer(): Unit = openBuffer(client)
  }

  "upon Join it response with list of buffers" in new FixtureWithSingleClient {
    client.send(welcomingActor, Join("cica"))
    val joinSuccess = client.expectMsgType[JoinSuccess]
    joinSuccess.bufferList shouldEqual List(bufferId)
  }

  "joined client receives OpenSuccess and list of users for Open" in new FixtureWithSingleClient {
    joinClient()
    client.send(welcomingActor, Open(bufferId))
    val openSuccess = client.expectMsgType[OpenSuccess]
    openSuccess.buffer shouldEqual "hali"

    val syncUsers = client.expectMsgType[SyncUserList]
    syncUsers.users shouldEqual List("cica")
  }

  "a joined client with opened buffer should receive its own command as sync" in new FixtureWithSingleClient {
    joinClient()
    openBuffer()
    client.send(welcomingActor, editMsg)
    client.expectMsg(Sync(bufferId, command, 0))
  }

  "multiple clients" - {
    trait FixtureWithTwoClients extends Fixture {
      val me = TestProbe("me")
      val other = TestProbe("other")
    }

    "other client receives my edit" in new FixtureWithTwoClients {
      joinClient(me, "me")
      openBuffer(me, "alma")

      joinClient(other, "other")
      openBuffer(other, "alma")

      me.send(welcomingActor, editMsg)
      other.expectMsg(Sync(bufferId, command, 0))
    }

    "other client receives userlist when I open the same buffer" in new FixtureWithTwoClients {
      joinClient(other, "other")
      openBuffer(other, "alma")

      joinClient(me, "me")
      openBuffer(me, "alma")

      other.expectMsgType[SyncUserList].users should contain theSameElementsAs List("me", "other")
    }

    "I receive userlist when I other client disconnects" in new FixtureWithTwoClients {
      joinClient(other, "other")
      openBuffer(other, "alma")

      joinClient(me, "me")
      openBuffer(me, "alma")

      other.send(other.ref, PoisonPill)
      me.expectMsgType[SyncUserList].users shouldEqual List("me")
    }
  }
}

class FakeWorkspace extends Workspace {
  private var content: String = "hali"

  override def listBuffers: List[String] = List("alma")
  override def persist(bufferId: BufferId, text: String): Unit = { content = text }
  override def load(bufferId: BufferId): String = content
}
