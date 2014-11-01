package auctionsystem

import akka.actor._
import akka.testkit._
import auctionsystem.Auction._
import auctionsystem.AuctionSystemMain.AuctionStarted
import auctionsystem.Buyer.{AuctionWon, AuctionOverbid, BidAccepted, BidRejected}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

class AuctionTest extends TestKit(ActorSystem("AuctionTest"))
with WordSpecLike
with BeforeAndAfterAll
with ImplicitSender
with Matchers {

  override def afterAll() {
    system.shutdown()
  }

  def fixture = {
    new {
      val auction = TestFSMRef(new Auction)
      val auctionProbe = TestProbe()
      auctionProbe watch auction

      val ignoredAuction = TestFSMRef(new Auction)
      ignoredAuction.setState(Auction.Ignored, Auction.Uninitialised)
      val ignoredAuctionProbe = TestProbe()
      ignoredAuctionProbe.watch(ignoredAuction)

    }
  }

  "Auction actor" when {
    "started" should {
      "call auction started event" in {
        val parent = TestProbe()
        val underTest = TestActorRef(Props[Auction], parent.ref, "Aution")

        parent.expectMsg(AuctionStarted(underTest.path.name))
      }

      "be in created state with uninitialised data" in {
        val auction = fixture.auction

        auction.stateName shouldEqual Auction.Created
        auction.stateData shouldEqual Auction.Uninitialised
      }
    }

    "in CREATED state" should {
      "receive bid timer after some time" ignore {
        // TODO
        val auction = fixture.auction
        val testProbe = fixture.auctionProbe

        within(13 seconds) {
          testProbe.expectMsg(BidTimer)
        }
      }

      "go to ignored state after bid timer" in {
        val auction = fixture.auction

        auction ! BidTimer
        auction.stateName shouldEqual Auction.Ignored
      }

      "accept any bid" in {
        val auction = fixture.auction
        auction ! new Bid(10)
        auction.stateName shouldEqual Auction.Activated
      }
    }

    "in IGNORED state" should {
      "receive delete timer" ignore {
        // TODO
        val auction = fixture.ignoredAuction
        val testProbe = fixture.ignoredAuctionProbe

        testProbe.expectMsgAllOf(5 seconds, DeleteTimer)
      }

      "be stopped after receiving DeleteTimer" in {
        val auction = fixture.ignoredAuction
        val auctionProbe = TestProbe()
        auctionProbe watch auction

        auction ! DeleteTimer

        auctionProbe.expectTerminated(auction)
      }

      "go to created state after relisted message received" in {
        val auction = fixture.ignoredAuction

        auction ! Auction.Relist

        auction.stateName shouldEqual Auction.Created
        auction.stateData shouldEqual Auction.Uninitialised
      }
    }

    "in sold state" should {
      "be stopped after receiving DeleteTimer" in {
        val auction = fixture.auction
        val auctionProbe = TestProbe()
        auctionProbe watch auction

        auction.setState(Auction.Ignored, Auction.Uninitialised)

        auction ! DeleteTimer

        auctionProbe.expectTerminated(auction)
      }
    }

    "in activated state" should {
      "receive bid timer" ignore {
        // TODO
        val auction = fixture.ignoredAuction
        val testProbe = fixture.ignoredAuctionProbe

        testProbe.expectMsgAllOf(5 seconds, DeleteTimer)
      }

      "respond with bidRejected when bid didn't overbid last bid " in {
        val auction = fixture.auction
        val auctionProbe = TestProbe()
        auctionProbe watch auction

        val buyer1 = TestProbe()
        val buyer2 = TestProbe()

        buyer1 send(auction, new Bid(10))
        buyer2 send(auction, new Bid(5))

        buyer2.expectMsg(BidRejected(auction, "Your bid is too low. Current bid: 10", 10)) // TODO any string matcher
      }

      "send overbid message to last buyer when he got overbid" in {
        val auction = fixture.auction
        val auctionProbe = TestProbe()
        auctionProbe watch auction

        val buyerOne = TestProbe()
        val buyerTwo = TestProbe()

        buyerOne.send(auction, new Bid(15))
        buyerTwo.send(auction, new Bid(20))

        buyerOne.expectMsg(BidAccepted(auction)) // TODO don't bother about first message
        buyerOne.expectMsg(AuctionOverbid(auction, 20))
      }

      "send error when one buyer overbids his own bid " in {
        val auction = fixture.auction
        val auctionProbe = TestProbe()
        auctionProbe watch auction

        val buyerOne = TestProbe()

        buyerOne.send(auction, new Bid(15))
        buyerOne.send(auction, new Bid(20))

        buyerOne.expectMsg(BidAccepted(auction)) // TODO don't bother about first message
        buyerOne.expectMsg(BidRejected(auction, "Your cannot overbid your own bid: 15", 15))
      }

      "send winning message to buyer who won auction" ignore { // TODO wypieprza się, ale jak się da rerun failed tests, to już działa...
        val auction = TestFSMRef(new Auction)

        auction ! new Bid(10)
        auction ! BidTimer
        expectMsg(BidAccepted(auction))
        expectMsg(AuctionWon(auction, 10)) // TODO to detailed message with price for which we bought auction
      }

      "should go to Sold state after auction ended" in {
        val auction = fixture.auction
        val auctionProbe = TestProbe()
        auctionProbe watch auction

        auction ! new Bid(10)
        auction ! BidTimer

        auction.stateName shouldEqual Auction.Sold
      }
    }
  }
}