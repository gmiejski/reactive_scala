package auctionsystem

import akka.actor.{Props, Actor}
import auctionsystem.Auction.{Bid, Relist}
import auctionsystem.Buyer.{BuyerAccount, MakeBid}
import scala.concurrent.duration._


object AuctionSystemMain {

  case class AuctionStarted(name: String)

}

class AuctionSystemMain extends Actor {

  import context.dispatcher

  val auction = context.actorOf(Props[Auction], "auction1")
  val auction2 = context.actorOf(Props[Auction], "auction2")

  val buyer1 = context.actorOf(Props(new Buyer(new BuyerAccount(100,2))), "buyer1")
  val buyer2 = context.actorOf(Props(new Buyer(new BuyerAccount(90,5))), "buyer2")
  val buyer3 = context.actorOf(Props(new Buyer(new BuyerAccount(30,3))), "buyer3")

  buyer1 ! new MakeBid(auction, 10)
  buyer2 ! new MakeBid(auction, 20)

  buyer3 ! new MakeBid(auction, 10)

  context.system.scheduler.scheduleOnce(4 seconds, auction, Relist)
  context.system.scheduler.scheduleOnce(10 seconds, auction, Bid)


  override def receive: Receive = {
    case msg => println(s"received: $msg")
  }
}
//
//import akka.actor._
//
//// (1) changed the constructor here
//class HelloActor(myName: String) extends Actor {
//  def receive = {
//    // (2) changed these println statements
//    case "hello" => println("hello from %s".format(myName))
//    case _       => println("'huh?', said %s".format(myName))
//  }
//}
//
//object Main extends App {
//  val system = ActorSystem("HelloSystem")
//  // (3) changed this line of code
//  val helloActor = system.actorOf(Props(new HelloActor("Fred")), name = "helloactor")
//  helloActor ! "hello"
//  helloActor ! "buenos dias"
//}
