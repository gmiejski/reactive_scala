package auctionsystem

import akka.actor.{Props, Actor, ActorRef, FSM}
import auctionsystem.Auction._
import auctionsystem.AuctionSystemMain.AuctionStarted
import auctionsystem.Buyer._

import scala.concurrent.duration._


object Auction {

  def props(auctionName: String): Props = Props(new Auction(auctionName))


  case class BidTimer()

  case class DeleteTimer()

  case class Relist()

  case class Bid(value: Int) {
    require(value > 0, "Bid value must be positive $ value!")
  }

  //States
  sealed trait AuctionState

  case object Created extends AuctionState

  case object Ignored extends AuctionState

  case object Activated extends AuctionState

  case object Sold extends AuctionState

  // Data
  sealed trait AuctionData

  case object Uninitialised extends AuctionData

  final case class WinningBid(buyer: ActorRef, bid: Int) extends AuctionData

}


class Auction(auctionName: String) extends Actor with FSM[AuctionState, AuctionData] {

  import akka.actor.Cancellable
  import context.dispatcher

  startWith(Created, Uninitialised)

  private var currentTimer: Cancellable = _

  override def preStart() {
    context.parent ! AuctionStarted(self.path.name)
    println("Prestart!!!!!!!!!")
    currentTimer = context.system.scheduler.scheduleOnce(10 seconds, self, BidTimer)
  }

  when(Created) {
    case Event(Bid(value), Uninitialised) =>
      sender ! new BidAccepted(auctionName, value)
      currentTimer.cancel()
      currentTimer = context.system.scheduler.scheduleOnce(7 seconds, self, BidTimer)
      goto(Activated) using WinningBid(sender(), value)
    case Event(BidTimer, Uninitialised) =>
      println("Auction going into ignored state: " + self.path.name)
      currentTimer = context.system.scheduler.scheduleOnce(3 seconds, self, DeleteTimer)
      goto(Ignored)
  }

  when(Ignored) {
    case Event(Relist, Uninitialised) =>
      println("Auction relisted: " + self.path.name)
      goto(Created)
    case Event(DeleteTimer, Uninitialised) =>
      println("Auction ended with no bid: " + self.path.name)
      stop()
  }

  when(Activated) {
    case Event(Bid(value), winningBid: WinningBid) =>
      println("#################")
      println("Current winning bid:" + winningBid.bid + " by " + winningBid.buyer.path.name)
      println("#################")

      if (sender().path == winningBid.buyer.path) {
        sender ! new SelfOverbidRejected(auctionName, "Your cannot overbid your own bid: " + winningBid.bid)
        stay() using winningBid
      }
      else if (value < winningBid.bid) {
        sender ! new BidRejected(auctionName, "Your bid is too low. Current bid: " + winningBid.bid, winningBid.bid)
        stay() using winningBid
      } else {
        sender ! new BidAccepted(auctionName, value)
        winningBid.buyer ! new AuctionOverbid(auctionName, value)
        stay() using WinningBid(sender(), value)
      }
    case Event(BidTimer, winningBid: WinningBid) =>
      println("Auction sold: " + auctionName)
      winningBid.buyer ! new AuctionWon(auctionName, winningBid.bid)
      currentTimer = context.system.scheduler.scheduleOnce(3 seconds, self, DeleteTimer)
      goto(Sold)
  }

  when(Sold) {
    case Event(DeleteTimer, w: WinningBid) =>
      stop()
  }

  initialize()
}
