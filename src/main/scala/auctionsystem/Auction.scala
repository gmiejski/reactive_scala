package auctionsystem

import akka.actor.Actor
import akka.event.LoggingReceive
import auctionsystem.Auction.{Bid, BidTimer, DeleteTimer}

import scala.concurrent.duration._

object Auction {
  case class BidTimer()
  case class DeleteTimer()
  case class Relist()
  case class Bid()
}


class Auction extends Actor {

  import context.dispatcher

  override def preStart() {
    context.parent ! "auction started: " +self.path.name
    context.system.scheduler.scheduleOnce(3 seconds, self, BidTimer)
  }

  def created: Receive = LoggingReceive {
    case Bid =>
      println("got bid!")
    case BidTimer =>
      println("bid timer")
  }

  def ignored: Receive = LoggingReceive {
    case DeleteTimer => print("O SHIET - auction ended")
  }

  def receive = created
}
