package integration

import akka.actor._
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import auctionsystem.Auction.BidTimer
import auctionsystem.Buyer._
import auctionsystem.search.AuctionSearch.{AuctionFound, AuctionNotFound}
import auctionsystem.search.{AuctionSearch, MasterSearchPartition}
import auctionsystem.{Auction, Buyer, Seller}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Success}

class PartitionRoutingIntegrationTest extends TestKit(ActorSystem("AuctionTest"))
with WordSpecLike
with BeforeAndAfterAll
with ImplicitSender
with Matchers {

  override def afterAll() {
    system.shutdown()
  }

  "Auction system" when {
    "started with many buyers and using partition routing strategy" should {
      "end with auction bought" in {
        val auctionSearch = TestActorRef(Props[MasterSearchPartition], "auctionSearch")

        val seller = TestActorRef(Props[Seller])

        val auction1 = TestActorRef(Auction.props("rower"))
        val auction2 = TestActorRef(Auction.props("Podróż życia"))
        val auction3 = TestActorRef(Auction.props("Adiday"))

        auctionSearch.tell(AuctionSearch.RegisterAuction("rower", auction1), seller)
        auctionSearch.tell(AuctionSearch.RegisterAuction("Podróż życia", auction2), seller)
        auctionSearch.tell(AuctionSearch.RegisterAuction("Adiday", auction3), seller)

        val buyer1 = TestActorRef(Buyer.props(new BuyerAccount(100, 2)), "buyer1")
        val buyer2 = TestActorRef(Buyer.props(new BuyerAccount(90, 5)), "buyer2")
        val buyer3 = TestActorRef(Buyer.props(new BuyerAccount(303, 33)), "buyer3")

        val testProbe = TestProbe()
        testProbe watch buyer3

        buyer1 ! new MakeBid("rower", 10)
        buyer1 ! new MakeBid("rower", 9)
        buyer3 ! new MakeBid("rower", 20)

        Thread.sleep(2000)
        implicit val timeout = Timeout(5 seconds)
        val auction = auctionSearch ? AuctionSearch.Search("rower")

        auction.onComplete {
          case Success(auctionFound: AuctionFound) =>
            auctionFound.auction ! BidTimer
            testProbe.expectMsg(AuctionWon)
          case Failure(error: AuctionNotFound) =>
            println("Auction not found: " + error.name + ". Not making any bid!")
        }
      }
    }

  }


}
