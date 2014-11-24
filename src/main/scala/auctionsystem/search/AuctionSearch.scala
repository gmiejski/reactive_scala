package auctionsystem.search

import akka.actor.{Actor, ActorRef}
import auctionsystem.search.AuctionSearch._

object AuctionSearch {

  case class Search(name: String)

  case class AuctionFound(auction: ActorRef, auctionName: String)

  case class AuctionNotFound(name: String) extends Throwable

  case class RegisterAuction(auctionName: String, auction: ActorRef)

  case class AuctionRegistered(auctionName: String, auction: ActorRef)

}

class AuctionSearch extends Actor {

  var auctions: Map[String, ActorRef] = Map()

  override def preStart() {
    println("Auction Search initialized :" + self.path)
  }

  override def receive: Receive = {
    case Search(auctionKeyword: String) =>
      println("search in actor : " + self.path.name)
      val singleWord: String = if (auctionKeyword.trim.indexOf(" ") == -1) auctionKeyword.trim() else auctionKeyword.trim.substring(auctionKeyword.indexOf(" "))
      println("auction keyword = " + singleWord)
      val auctionFullName: Option[String] = auctions.keys.find(_.contains(auctionKeyword))

      if (auctionFullName.isDefined) {
        sender() ! AuctionFound(auctions.apply(auctionFullName.get), auctionFullName.get)
      } else {
        sender() ! AuctionNotFound(auctionKeyword)
      }

    case RegisterAuction(auctionName: String, auction: ActorRef) =>
      println("registering auction in actor : " + self.path.name + ". Auction name = " + auctionName)
      auctions = auctions.+(auctionName -> auction)
      sender() ! AuctionRegistered(auctionName, auction)
  }
}
