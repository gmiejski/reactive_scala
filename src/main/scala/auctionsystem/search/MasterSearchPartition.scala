package auctionsystem.search

import akka.actor._
import akka.routing._

class MasterSearchPartition extends Actor {

  var auctionSearchers: List[ActorRef] = List()

  val routees = Vector.fill(5) {
    val r = context.actorOf(Props[AuctionSearch])
    auctionSearchers = auctionSearchers ::: List(r)
    context watch r
    ActorRefRoutee(r)
  }

  var router = {
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def receive = {
    case x: AuctionSearch.Search =>
      println("MASTER SEARCH: Searching")
      val searchHelper = context.actorOf(SearchHelper.props(router, sender()))
      searchHelper ! x
    case x: AuctionSearch.RegisterAuction =>
      println("MASTER SEARCH : register")
      router.route(x, sender())
    case Terminated(a) =>
      router = router.removeRoutee(a)
      val r = context.actorOf(Props[AuctionSearch])
      context watch r
      router = router.addRoutee(r)
  }
}
