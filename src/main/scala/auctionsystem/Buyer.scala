package auctionsystem

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import auctionsystem.Auction.Bid
import auctionsystem.AuctionSearch.{AuctionFound, AuctionNotFound}
import auctionsystem.Buyer._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util._

object Buyer {

  //constructor

  def props(account: BuyerAccount): Props = Props(new Buyer(account))

  //messages

  case class BidRejected(auctionName: String, reason: String, winningOffer: Int)

  case class SelfOverbidRejected(auctionName: String, reason: String)

  case class BidAccepted(auctionName: String, bid: Int)

  case class MakeBid(auction: String, bid: Int)

  case class AuctionOverbid(auctionName: String, bid: Int)

  case class AuctionWon(auctionName: String, bid: Int)

  //state
  sealed trait BuyerState

  case object BuyerNormalState extends BuyerState

  //data

  sealed trait BuyerData

  case class BuyerAccount(max: Int, bidOver: Int) extends BuyerData

}

class Buyer(account: BuyerAccount) extends Actor with FSM[BuyerState, BuyerData] {

  startWith(BuyerNormalState, account)

  var auctionSearch: ActorRef = _

  override def preStart() {
    val actorSelection: ActorSelection = context.actorSelection("../auctionSearch")
    implicit val timeout = Timeout(5 seconds)
    actorSelection.resolveOne(2 seconds).onComplete {
      case Success(actorRef: ActorRef) =>
        auctionSearch = actorRef
      case Failure(t: ActorNotFound) =>
        println("An error has occured during actor retrieving: " + t.getMessage)
        println("buyer: " + self.path.name + " STOPPED!!!!!!!!!!!!!!!!!")
        stop()
    }
  }

  when(BuyerNormalState) {
    case Event(MakeBid(auctionName, bid), account: BuyerAccount) =>
      implicit val timeout = Timeout(5 seconds)
      val auction = auctionSearch ? AuctionSearch.Search(auctionName)

      auction.onComplete {
        case Success(auctionFound: AuctionFound) =>
          if (bid <= account.max) {
            println(self.path.name + " making new bid for: " + auctionFound.auctionName + " with bid = " + bid)
            auctionFound.auction ! new Bid(bid)
          }
        case Failure(error: AuctionNotFound) =>
          println("Auction not found: " + error.name + ". Not making any bid!")
      }
      stay()

    case Event(BidRejected(auctionName, reason, winningOffer), account: BuyerAccount) =>
      val name = self.path.name
      println(s"$name's bid for auction $auctionName :rejected for reason: " + reason)
      self ! new MakeBid(auctionName, winningOffer + account.bidOver)
      stay()

    case Event(SelfOverbidRejected(auctionName, reason), account: BuyerAccount) =>
      val name = self.path.name
      println(s"$name's bid for auction $auctionName :rejected for reason: " + reason)
      stay()

    case Event(BidAccepted(auctionName, bid), account: BuyerAccount) =>
      val name = self.path.name
      println(s"$name's bid for auction $auctionName accepted with bid = $bid")
      stay()

    case Event(AuctionOverbid(auctionName, bid), account: BuyerAccount) =>
      val name = self.path.name
      println(s"$name's bid for auction $auctionName has been overbid to :" + bid)
      if (bid + account.bidOver > account.max) {
        println(self.path.name + s"Lost auction: $auctionName, where bid is now: " + bid)
      } else {
        self ! new MakeBid(auctionName, bid + account.bidOver)
      }
      stay()

    case Event(AuctionWon(auctionName, bid), account: BuyerAccount) =>
      val name = self.path.name
      println(s"$name has won auction $auctionName for :" + bid)
      stay()
  }
}
