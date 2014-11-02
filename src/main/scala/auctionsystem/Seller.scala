package auctionsystem

import akka.actor._
import auctionsystem.AuctionSearch.AuctionRegistered
import auctionsystem.Seller._

import scala.util.{Failure, Success}
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

object Seller {

  case class AuctionEnded(auction: ActorRef, maxBid: Option[Int], buyer: Buyer)

  sealed trait SellerState

  case object NormalState extends SellerState

  sealed trait SellerData

  case class SellerAccount(money: Int) extends SellerData

  def props(auctions: String*): Props = Props(new Seller(auctions: _*))
}


class Seller extends FSM[SellerState, SellerData] {

  var currentAuctions: List[ActorRef] = _
  var historyAuctions: List[ActorRef] = _

  var auctionSearch: ActorRef = _

  def this(auctionNames: String*) = {
    this()
    currentAuctions = auctionNames.toList.map(x => context.actorOf(Auction.props(x)))
    historyAuctions = List()
  }

  startWith(NormalState, SellerAccount(0))

  override def preStart() {
    val actorSelection: ActorSelection = context.actorSelection("/user/app/auctionSearch")

    actorSelection.resolveOne(2 seconds).onComplete {
      case Success(actorRef: ActorRef) =>
        println("seller: " + self.path.name + " got auctionSearch")
        auctionSearch = actorRef
      case Failure(t: ActorNotFound) =>
        println("An error has occured during actor retrieving: " + t.getMessage)
        println("seller: " + self.path.name + " STOPPED!!!!!!!!!!!!!!!!!")
        stop()
    }
  }

  when(NormalState) {
    case Event(AuctionEnded(auction: ActorRef, maxBid: Option[Int], buyer: Buyer), account: SellerAccount) =>
      archiveAuction(auction)
      stay() using SellerAccount(account.money + maxBid.getOrElse(0))
    case Event(AuctionRegistered(auctionName: String, auction: ActorRef), account: SellerAccount) =>
      println(s"Auction registered : $auctionName")
      stay()
  }

  def archiveAuction(auction: ActorRef) = {
    val foundAuctionRef: Option[ActorRef] = currentAuctions.find(_.path.equals(auction.path))
    if (foundAuctionRef.isDefined) {
      currentAuctions = currentAuctions.filterNot(_.path.equals(auction.path))
      //      historyAuctions = List(historyAuctions: _*, foundAuctionRef.get)
    }
  }

  initialize()
}
