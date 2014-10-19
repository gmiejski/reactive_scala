package auctionsystem

import akka.actor.{Actor, Props}
import auctionsystem.Auction.Bid

class AuctionSystemMain extends Actor{

  val auction = context.actorOf(Props[Auction], "auction1")

  auction ! Bid

  override def receive: Receive = {
     case msg => println(s"received: $msg")
  }
}
