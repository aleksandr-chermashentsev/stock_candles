package testtask.actors

import java.util.Date

/**
 *
 * @author a.chermashentsev
 *         Date: 25.07.2019
 **/
object Messages {

  case class MarketData(timestamp:Long, ticker:String, price:Double, size: Int)
  case class Start()
  case class GiveLastCandles(num: Int)
  case class Tick()
  case class Candle(ticker: String, timestamp: Date, open: Double, high: Double, low: Double, close: Double, volume: Int)

}
