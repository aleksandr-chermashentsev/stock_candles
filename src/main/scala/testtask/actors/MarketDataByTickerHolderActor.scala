package testtask.actors

import java.util.Date
import java.util.concurrent.TimeUnit

import testtask.actors.Messages.{Candle, GiveLastCandles, MarketData}
import akka.actor.{Actor, Props}

import scala.collection.immutable.TreeMap

/**
  * Receives only market data by specific ticker.
  * One actor per ticker
  **/
class MarketDataByTickerHolderActor extends Actor {
  var marketData: TreeMap[Long, MarketData] = TreeMap[Long, MarketData]()(implicitly[Ordering[Long]].reverse)

  override def receive: Receive = {

    case x: MarketData => println(s"Actor=$self data=$x")
      marketData += (x.timestamp -> x)
    case GiveLastCandles(num) =>
      extractCandles(num).foreach {
        case Some(candle) => sender() ! candle
      }
  }

  def extractCandles(num: Int): Iterable[Option[Candle]] = {
    def removeMinutesFromTimestamp(data: MarketData) = {
      data.timestamp - (data.timestamp % TimeUnit.MINUTES.toMillis(1))
    }

    marketData.values
      .to(LazyList)
      .groupBy((data: MarketData) => removeMinutesFromTimestamp(data))
      .map((tuple: (Long, Iterable[MarketData])) =>
        tuple._2.foldLeft(Option.empty[Candle])((left, data) => {
          left match {
            case None =>
              Some(
                Candle(
                  data.ticker,
                  new Date(tuple._1),
                  data.price,
                  data.price,
                  data.price,
                  data.price,
                  1)
              )
            case Some(c: Candle) =>
              Some(c.copy(
                high = Math.max(c.high, data.price),
                low = Math.min(c.low, data.price),
                close = data.price,
                volume = c.volume + 1
              ))
          }
        }))
      .take(num)
  }
}

object MarketDataByTickerHolderActor {
  def props: Props = Props(new MarketDataByTickerHolderActor)
}
