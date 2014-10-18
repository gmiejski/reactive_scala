package auctionsystem

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Props}
import akka.event.LoggingReceive
import auctionsystem.BidTimer.{Expired, Start}

import scala.concurrent.duration._

object BidTimer {

  case class Start()

  case class Expired()

}

class DeleteTimer extends Actor {
  def receive = {
    case "start" =>
      print( "delete timer started")
//      context.system.scheduler.scheduleOnce(Duration.create(5, TimeUnit.SECONDS), sender, "expired");
  }
}

class Auction extends Actor {

  def notcreated: Receive = LoggingReceive {
    case Start =>
      sender ! "auction started"
      context become created
  }

  def created: Receive = LoggingReceive {
    case Expired =>
      val deleteTimer = context.actorOf(Props[DeleteTimer], "deleteTimer1")
      deleteTimer ! "start"
      context become ignored
  }

  def ignored: Receive = LoggingReceive {
    case "expired" => print("O SHIET")
  }

  def receive = notcreated
}
