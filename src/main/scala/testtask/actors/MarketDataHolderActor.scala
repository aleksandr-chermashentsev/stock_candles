package testtask.actors

import akka.actor.{Actor, Props}
import testtask.actors.Messages.MarketData

/**
 * Get Market data from Netty server
 **/
class MarketDataHolderActor extends Actor {
  override def receive: Receive = {
    case x: MarketData => {
      context.child(x.ticker) match {
        case Some(a) => a ! x
        case None => context.actorOf(MarketDataByTickerHolderActor.props, x.ticker) ! x
      }
    }
  }
}

object MarketDataHolderActor {
  def props: Props = Props(new MarketDataHolderActor)
}
