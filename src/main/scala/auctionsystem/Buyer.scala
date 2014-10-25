package auctionsystem

import akka.actor.{ActorRef, Actor}
import auctionsystem.Auction.Bid
import auctionsystem.Buyer._

object Buyer {

  case class BidRejected(auction: ActorRef, reason: String, winningOffer: Int)

  case class BidAccepted(auction: ActorRef)

  case class MakeBid(auction: ActorRef, bid: Int)

  case class BuyerAccount(max: Int, bidOver: Int)

  case class AuctionOverbid(auction: ActorRef, bid: Int)

  case class AuctionWon(auction: ActorRef, bid: Int)

}

class Buyer(account: BuyerAccount) extends Actor {

  override def receive: Receive = {
    case makeBid: MakeBid =>
      if (makeBid.bid <= account.max) {
        println(self.path.name + "Making new bid for: " + makeBid.auction.path.name + "with bid = " + makeBid.bid)
        makeBid.auction ! new Bid(makeBid.bid)
      }

    case rejection: BidRejected =>
      val name = self.path.name
      val auctionName = rejection.auction.path.name
      println(s"$name's bid for auction $auctionName :rejected for reason: " + rejection.reason)
      self ! new MakeBid(rejection.auction, rejection.winningOffer + account.bidOver)

    case accepted: BidAccepted =>
      val name = self.path.name
      val auctionName = accepted.auction.path.name
      println(s"$name's bid for auction $auctionName accepted")

    case overbid: AuctionOverbid =>
      val name = self.path.name
      val auctionName = overbid.auction.path.name
      println(s"$name's bid for auction $auctionName has been overbid to :" + overbid.bid)
      if (overbid.bid + account.bidOver > account.max) {
        println(self.path.name + s"Lost auction: $auctionName, where bid is now: " + overbid.bid)
      } else {
        self ! new MakeBid(overbid.auction, overbid.bid + account.bidOver)
      }

    case won: AuctionWon =>
      val name = self.path.name
      val auctionName = won.auction.path.name
      println(s"$name's has won auction $auctionName for :" + won.bid)
  }

}
