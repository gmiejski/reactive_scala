package auctionsystem.search

import akka.actor._
import akka.routing.{Broadcast, Router}
import akka.util.Timeout
import auctionsystem.search.SearchHelper.SearchTimeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object SearchHelper {

  def props(auctionSearchers: Router, buyer: ActorRef) = Props(new SearchHelper(auctionSearchers, buyer))

  case object SearchTimeout

}

class SearchHelper extends Actor {

  var auctionSearchRouter: Router = _
  var buyer: ActorRef = _
  var auctionName: String = _

  var numberOfNotFoundAnswers: Int = 0
  var maxAnswers: Int = _

  private var timer: Cancellable = _

  def this(auctionSearchRouter: Router, buyer: ActorRef) = {
    this()
    this.auctionSearchRouter = auctionSearchRouter
    this.buyer = buyer
    this.maxAnswers = auctionSearchRouter.routees.size
    println(auctionSearchRouter.routees.size)
  }

  def fireAction(auctionName: String) = {
    implicit val timeout = Timeout(10 seconds)

    timer = context.system.scheduler.scheduleOnce(10 seconds, self, SearchTimeout)

    auctionSearchRouter.route(Broadcast(AuctionSearch.Search(auctionName)), self)
  }

  override def receive = {
    case AuctionSearch.Search(auctionName: String) =>
      println("Search HELPER : Search!")
      fireAction(auctionName)
    case AuctionSearch.AuctionFound(auction: ActorRef, auctionName: String) =>
      println("Search HELPER : Auction found!")
      notifyAuctionFound(auction, auctionName)
    case AuctionSearch.AuctionNotFound(auctionName: String) =>
      println("Search HELPER : Auction NOT found!")
      numberOfNotFoundAnswers += 1
      if (numberOfNotFoundAnswers == maxAnswers) {
        notifyNoAuctionFound(auctionName)
      }
    case SearchTimeout =>
      println("Search HELPER : TIMEOUT!")
      notifyNoAuctionFound(auctionName)
  }

  def notifyAuctionFound(auction: ActorRef, auctionName: String) = {
    timer.cancel()
    buyer ! AuctionSearch.AuctionFound(auction, auctionName)
    self ! PoisonPill
  }

  def notifyNoAuctionFound(auctionName: String) = {
    buyer ! AuctionSearch.AuctionNotFound(auctionName)
    self ! PoisonPill
  }
}
