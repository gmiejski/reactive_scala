package auctionsystem

import akka.actor.{Actor, ActorNotFound, ActorRef, ActorSelection}
import akka.io.Tcp.Register
import auctionsystem.AuctionSearch._
import auctionsystem.AuctionSystemMain.AuctionStarted


import scala.util.{Failure, Success}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object AuctionSearch {

  case class Search(name: String)

  case class AuctionFound(auction: ActorRef, auctionName: String)

  case class AuctionNotFound(name: String) extends Throwable

  case class RegisterAuction(auction: String)

  case class AuctionRegistered(auctionName: String, auction: ActorRef)

}

class AuctionSearch extends Actor {

  var auctions: Map[String, ActorRef] = Map()

  override def preStart() {
    println("Auction Search initialized :" + self.path)
  }

  override def receive: Receive = {
    case Search(auctionKeyword: String) =>
      val singleWord: String = if (auctionKeyword.trim.indexOf(" ") == -1) auctionKeyword.trim() else auctionKeyword.trim.substring(auctionKeyword.indexOf(" "))
      println("auction keyword = " + singleWord)
      val auctionFullName: Option[String] = auctions.keys.find(_.contains(auctionKeyword))

      if (auctionFullName.isDefined) {
        sender() ! AuctionFound(auctions.apply(auctionFullName.get), auctionFullName.get)
      } else {
        sender() ! AuctionNotFound(auctionKeyword)
      }

    case RegisterAuction(auctionName: String) =>
      val auction = context.actorOf(Auction.props(auctionName)) // TODO add name to actorRef and check if can register such name
      auctions = auctions.+(auctionName -> auction)
      sender() ! AuctionRegistered(auctionName, auction)
  }
}
