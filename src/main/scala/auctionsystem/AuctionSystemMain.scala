package auctionsystem

import akka.actor._
import auctionsystem.Buyer.{BuyerAccount, MakeBid}
import auctionsystem.search.{AuctionSearch, MasterSearchReplication}

object AuctionSystemMain {

  case class AuctionStarted(name: String)

}

class AuctionSystemMain extends Actor {

  //  val auction = context.actorOf(Auction.props("auction1"), "auction1")

  val masterSearchReplication = context.actorOf(Props[MasterSearchReplication], "auctionSearch")

  val seller = context.actorOf(Seller.props("rower", "Podróż życia", "Adidasy"))

  val auction1 = context.actorOf(Auction.props("rower"), "auction1")
  val auction2 = context.actorOf(Auction.props("Podróż życia"), "auction2")
  val auction3 = context.actorOf(Auction.props("Adiday"), "auction3")

  masterSearchReplication.tell(AuctionSearch.RegisterAuction("rower", auction1), seller)
  masterSearchReplication.tell(AuctionSearch.RegisterAuction("Podróż życia", auction2), seller)
  masterSearchReplication.tell(AuctionSearch.RegisterAuction("Adiday", auction3), seller)

  //  auctionSearch.tell(AuctionSearch.RegisterAuction("rower"), seller)
  //  auctionSearch.tell(AuctionSearch.RegisterAuction("Podróż życia"), seller)
  //  auctionSearch.tell(AuctionSearch.RegisterAuction("Adiday"), seller)

  val buyer1 = context.actorOf(Buyer.props(new BuyerAccount(100, 2)), "buyer1")
  val buyer2 = context.actorOf(Buyer.props(new BuyerAccount(90, 5)), "buyer2")
  val buyer3 = context.actorOf(Buyer.props(new BuyerAccount(303, 33)), "buyer3")

  buyer1 ! new MakeBid("rower", 10)
  buyer2 ! new MakeBid("rower", 9)
  buyer3 ! new MakeBid("rower", 20)
  buyer3 ! new MakeBid("życia", 10)

  //  context.system.scheduler.scheduleOnce(4 seconds, auction2, Relist)

  override def receive: Receive = {
    case msg => println(s"received: $msg")
  }
}

