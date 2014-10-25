package auctionsystem

import akka.actor.{FSM, ActorRef, Actor}
import auctionsystem.Auction.Bid
import auctionsystem.Buyer._

object Buyer {

  case class BidRejected(auction: ActorRef, reason: String, winningOffer: Int)

  case class BidAccepted(auction: ActorRef)

  case class MakeBid(auction: ActorRef, bid: Int)

  case class AuctionOverbid(auction: ActorRef, bid: Int)

  case class AuctionWon(auction: ActorRef, bid: Int)

  //state
  sealed trait BuyerState

  case object BuyerNormalState extends BuyerState

  //data

  sealed trait Data

  case class BuyerAccount(max: Int, bidOver: Int) extends Data

}

class Buyer(account: BuyerAccount) extends Actor with FSM[BuyerState, Data] {

  startWith(BuyerNormalState, account)

  when(BuyerNormalState) {
    case Event(MakeBid(auction, bid), account: BuyerAccount) =>
      if (bid <= account.max) {
        println(self.path.name + "Making new bid for: " + auction.path.name + "with bid = " + bid)
        auction ! new Bid(bid)
      }
      stay()
    case Event(BidRejected(auction, reason, winningOffer), account: BuyerAccount) =>
      val name = self.path.name
      val auctionName = auction.path.name
      println(s"$name's bid for auction $auctionName :rejected for reason: " + reason)
      self ! new MakeBid(auction, winningOffer + account.bidOver)
      stay()

    case Event(BidAccepted(auction), account: BuyerAccount) =>
      val name = self.path.name
      val auctionName = auction.path.name
      println(s"$name's bid for auction $auctionName accepted")
      stay()

    case Event(AuctionOverbid(auction, bid), account: BuyerAccount) =>
      val name = self.path.name
      val auctionName = auction.path.name
      println(s"$name's bid for auction $auctionName has been overbid to :" + bid)
      if (bid + account.bidOver > account.max) {
        println(self.path.name + s"Lost auction: $auctionName, where bid is now: " + bid)
      } else {
        self ! new MakeBid(auction, bid + account.bidOver)
      }
      stay()

    case Event(AuctionWon(auction, bid), account: BuyerAccount) =>
      val name = self.path.name
      val auctionName = auction.path.name
      println(s"$name's has won auction $auctionName for :" + bid)
      stay()
  }
}
