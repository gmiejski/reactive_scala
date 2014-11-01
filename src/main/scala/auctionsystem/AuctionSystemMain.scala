package auctionsystem

import akka.actor.{Actor, Props}
import auctionsystem.Buyer.{BuyerAccount, MakeBid}


object AuctionSystemMain {

  case class AuctionStarted(name: String)

}

class AuctionSystemMain extends Actor {

  val auction = context.actorOf(Props[Auction], "auction1")
//  val auction2 = context.actorOf(Props[Auction], "auction2")

  // TODO - bidy dopiero po tym jak dostanie się że akcja jest rozpoczęta
  val buyer1 = context.actorOf(Buyer.props(new BuyerAccount(100,2)), "buyer1")
  val buyer2 = context.actorOf(Buyer.props(new BuyerAccount(90,5)), "buyer2")
  val buyer3 = context.actorOf(Buyer.props(new BuyerAccount(303,33)), "buyer3")

  buyer1 ! new MakeBid(auction, 10)
  buyer2 ! new MakeBid(auction, 20)

  buyer3 ! new MakeBid(auction, 10)

//  context.system.scheduler.scheduleOnce(4 seconds, auction2, Relist)

  override def receive: Receive = {
    case msg => println(s"received: $msg")
  }
}

