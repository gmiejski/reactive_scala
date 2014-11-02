package auctionsystem

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import auctionsystem.AuctionSearch.Search
import auctionsystem.Buyer.{MakeBid, BuyerAccount}

import scala.util.{Failure, Success}
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

object AuctionSystemMain {

  case class AuctionStarted(name: String)

}

class AuctionSystemMain extends Actor {

  //  val auction = context.actorOf(Auction.props("auction1"), "auction1")
  //  val auction2 = context.actorOf(Props[Auction], "auction2")

  val auctionSearch = context.actorOf(Props[AuctionSearch], "auctionSearch")


  val seller = context.actorOf(Props[Seller])

  auctionSearch.tell(AuctionSearch.RegisterAuction("rower"), seller)
  auctionSearch.tell(AuctionSearch.RegisterAuction("Podróż życia"), seller)

  val buyer1 = context.actorOf(Buyer.props(new BuyerAccount(100, 2)), "buyer1")
//  val buyer2 = context.actorOf(Buyer.props(new BuyerAccount(90, 5)), "buyer2")
//  val buyer3 = context.actorOf(Buyer.props(new BuyerAccount(303, 33)), "buyer3")

  buyer1 ! new MakeBid("rower", 10)
  buyer1 ! new MakeBid("rower", 9)
//  buyer2 ! new MakeBid("rower", 20)
//  buyer3 ! new MakeBid("rower", 10)

  //  val actorSelection: ActorSelection = context.actorSelection("/user/auctionSearch")

  //  actorSelection.resolveOne(2 seconds).onComplete {
  //    case Success(actorRef: ActorRef) =>
  //      print("success")
  //      x = Option.apply(actorRef)
  //    case Success(actorRef: ActorNotFound) =>
  //      print("success")
  //      x = Option.apply(actorRef)
  //    case Failure(t: ActorNotFound) =>
  //      println("An error has occured during actor retrieving: " + t.getMessage)
  //      println(s"Coulndt retrieve actor with name: $singleWord !")
  //  }


  //  implicit val timeout = Timeout(5 seconds)
  //  val future = auctionSearch ? new Search("buyer1")
  //  future.onComplete {
  //    case Success(x: AuctionSearch.SearchResult) => println("got buyer with path: " + x.auction.get.path)
  //  }


  //
  //  buyer3 ! new MakeBid(auction, 10)

  //  context.system.scheduler.scheduleOnce(4 seconds, auction2, Relist)

  override def receive: Receive = {
    case msg => println(s"received: $msg")
  }
}

