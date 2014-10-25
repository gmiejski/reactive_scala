package auctionsystem

import auctionsystem.Auction._
import auctionsystem.AuctionSystemMain.AuctionStarted
import auctionsystem.Buyer.{AuctionWon, AuctionOverbid, BidAccepted, BidRejected}

import scala.concurrent.duration._
import akka.actor.{Actor, ActorRef, FSM}


object Auction {

  case class BidTimer()

  case class DeleteTimer()

  case class Relist()

  case class Bid(value: Int)

  //States
  sealed trait AuctionState

  case object Created extends AuctionState

  case object Ignored extends AuctionState

  case object Activated extends AuctionState

  case object Sold extends AuctionState

  // Data
  sealed trait Data

  case object Uninitialized extends Data

  private final case class WinningBid(buyer: ActorRef, bid: Int) extends Data

}


class Auction extends Actor with FSM[AuctionState, Data] {

  startWith(Created, Uninitialized)

  override def preStart() {
    context.parent ! AuctionStarted(self.path.name)
  }

  when(Created, stateTimeout = 10 seconds) {
    case Event(Bid(value), Uninitialized) =>
      sender ! new BidAccepted(self)
      goto(Activated) using WinningBid(sender, value)
    case Event(StateTimeout, Uninitialized) =>
      println("Auction going into ignored state: " + self.path.name)
      goto(Ignored)
  }

  when(Ignored, stateTimeout = 3 seconds) {
    case Event(StateTimeout, Uninitialized) =>
      println("Auction ended with no bid: " + self.path.name)
      stop()
    case Event(Relist, Uninitialized) =>
      println("Auction relisted: " + self.path.name)
      goto(Created)
  }

  when(Activated, stateTimeout = 3 seconds) {
    case Event(Bid(value), winningBid: WinningBid) =>

      println("#################")
      println("Current winning bid:" + winningBid.bid + " by " + winningBid.buyer.path.name)
      println("#################")

      if (value < 0) {
        sender ! new BidRejected(self, "Your bid cannot be lower than 0!", winningBid.bid)
        stay() using winningBid
      }
      if (value < winningBid.bid) {
        sender ! new BidRejected(self, "Your bid is too low. Current bid: " + winningBid.bid, winningBid.bid)
        stay() using winningBid
      } else {
        sender ! new BidAccepted(self)
        winningBid.buyer ! new AuctionOverbid(self, value)
        stay() using WinningBid(sender, value)
      }

    case Event(StateTimeout, winningBid: WinningBid) =>
      println("Auction sold: " + self.path.name)
      winningBid.buyer ! new AuctionWon(self, winningBid.bid)
      goto(Sold)
  }

  when(Sold, stateTimeout = 5 seconds) {
    case Event(StateTimeout, w: WinningBid) =>
      stop()
  }

  initialize()
}
