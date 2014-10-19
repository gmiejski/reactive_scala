package auctionsystem

import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorSystem, Actor}
import akka.testkit.{TestProbe, ImplicitSender, TestKit, TestActorRef, TestActor}
import auctionsystem.Auction.BidTimer
import auctionsystem.AuctionSystemMain.AuctionStarted
import org.scalatest.matchers.MustMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpecLike, WordSpec}

import scala.concurrent.duration.FiniteDuration


class AuctionTest extends TestKit(ActorSystem("testAuctionSystem"))
with WordSpecLike
with ImplicitSender
with MustMatchers
with BeforeAndAfterAll {

  override def afterAll() {
    system.shutdown()
  }

  "Auction actor " should {
    "fire bidTimer" in {

      val actorRef = TestActorRef[Auction]
      actorRef.start()

      within(new FiniteDuration(5, TimeUnit.SECONDS)) {
        expectMsg(BidTimer)
      }
    }
  }
}
