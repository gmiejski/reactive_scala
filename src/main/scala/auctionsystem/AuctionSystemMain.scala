package auctionsystem

import akka.actor.{Actor, Props}
import auctionsystem.BidTimer.Start

class AuctionSystemMain extends Actor{

  val auction = context.actorOf(Props[Auction], "auction1")

  auction ! Start

  override def receive: Receive = {
     case msg => print(s"received: $msg")
  }
}
