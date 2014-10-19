package auctionsystem

import akka.actor.{ActorRef, Actor}
import akka.event.LoggingReceive
import auctionsystem.Auction._
import auctionsystem.AuctionSystemMain.AuctionStarted
import auctionsystem.Buyer.{AuctionOverbid, BidAccepted, BidRejected}

import scala.concurrent.duration._

object Auction {

  case class BidTimer()

  case class DeleteTimer()

  case class Relist()

  case class Bid(value: Int)

  case class WinningBid(buyer: ActorRef, bid: Int)

}


class Auction extends Actor {

  private var winningBid: WinningBid = _

  import context.dispatcher

  override def preStart() {
    context.parent ! AuctionStarted(self.path.name)
    scheduleBidTimer(30 seconds)
  }

  def created: Receive = LoggingReceive {
    case bid: Bid =>
      sender ! new BidAccepted(self)
      winningBid = new WinningBid(sender, bid.value)
      context become activated
    case BidTimer =>
      println("bid timer")
      context.system.scheduler.scheduleOnce(2 seconds, self, DeleteTimer)
      context become ignored
  }

  def ignored: Receive = LoggingReceive {
    case DeleteTimer =>
      println("Auction ended with no bid: " + self.path.name)
      context.stop(self)
    case Relist =>
      println("Auction relisted: " + self.path.name)
      scheduleBidTimer(3 seconds)
      context become created
  }

  def activated: Receive = LoggingReceive {
    case bid: Bid =>
      if (bid.value < 0) {
        sender ! new BidRejected(self, "Your bid cannot be lower than 0!", winningBid.bid)
      }
      if (bid.value < winningBid.bid) {
        sender ! new BidRejected(self, "Your bid is too low. Current bid: " + winningBid.bid, winningBid.bid)
      } else {
        sender ! new BidAccepted(self)
        winningBid.buyer ! new AuctionOverbid(self, bid.value)
        winningBid = new WinningBid(sender, bid.value)
      }
      scheduleBidTimer()
    case BidTimer =>
      println("Auction sold: " + self.path.name)
      //TODO notify
      context become sold
  }

  def sold: Receive = LoggingReceive {
    case DeleteTimer =>
      context.stop(self)
  }

  def receive = created

  private def scheduleBidTimer(delay: FiniteDuration = 3.seconds) {
    context.system.scheduler.scheduleOnce(delay, self, BidTimer)
  }
}
