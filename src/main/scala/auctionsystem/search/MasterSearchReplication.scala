package auctionsystem.search

import akka.actor.{Actor, Props, Terminated}
import akka.routing._

class MasterSearchReplication extends Actor {

  val routees = Vector.fill(5) {
    val r = context.actorOf(Props[AuctionSearch])
    context watch r
    ActorRefRoutee(r)
  }

  var router = {
    Router(RoundRobinRoutingLogic(), routees)
  }

  //  val router = context.actorOf(Props[AuctionSearch].
  //    withRouter(RoundRobinRouter(5)), name = "masterSearchRouter")

  override def receive: Receive = {
    case x: AuctionSearch.Search =>
      println("MASTER SEARCH: Searching")
      router.route(x, sender())
    case x: AuctionSearch.RegisterAuction =>
      println("MASTER SEARCH : register")
      router.route(Broadcast(x), sender())
    case Terminated(a) =>
      router = router.removeRoutee(a)
      val r = context.actorOf(Props[AuctionSearch])
      context watch r
      router = router.addRoutee(r)

  }
}
