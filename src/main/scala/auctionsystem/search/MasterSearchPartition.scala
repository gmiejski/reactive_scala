package auctionsystem.search

import akka.actor._
import akka.routing._
import akka.util.Timeout
import auctionsystem.search.AuctionSearch.{RegisterAuction, Search}

import scala.concurrent.duration._

class MasterSearchPartition extends Actor {

  val routees = Vector.fill(5) {
    val r = context.actorOf(Props[AuctionSearch])
    context watch r
    ActorRefRoutee(r)
  }

  //  var router = {
  //    Router(RoundRobinRoutingLogic(), routees)
  //  }

  val router = context.actorOf(Props[AuctionSearch].
    withRouter(RoundRobinRouter(5)), name = "masterSearchRouter")


  override def receive = {
    case x: Search =>
      implicit val timeout = Timeout(5 seconds)
    //      router.route(Broadcast(x), sender())

    case x: RegisterAuction =>
      //      router.route(x, sender())
      ???
    //    case Terminated(a) =>
    //      router = router.removeRoutee(a)
    //      val r = context.actorOf(Props[AuctionSearch])
    //      context watch r
    //      router = router.addRoutee(r)
  }
}
