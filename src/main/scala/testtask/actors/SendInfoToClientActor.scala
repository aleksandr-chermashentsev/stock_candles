package testtask.actors

import java.text.SimpleDateFormat
import java.util.Date

import testtask.actors.Messages.{Candle, GiveLastCandles, Start, Tick}
import akka.actor.{Actor, PoisonPill, Props}
import io.netty.channel.ChannelHandlerContext
import spray.json._
import DefaultJsonProtocol.{jsonFormat4, _}
import testtask.json.MyJsonProtocol._

import scala.util.Try


/**
  * Sends candles to client.
  * One actor per connection.
  * Dies when client disconnects
  **/
class SendInfoToClientActor(channel: ChannelHandlerContext) extends Actor {

  override def receive: Receive = {
    case Start => context.actorSelection("/user/marketData/*") ! GiveLastCandles(10)
    case c: Candle => channel.writeAndFlush(c.toJson.toString() + "\n")
    case Tick => context.actorSelection("/user/marketData/*") ! GiveLastCandles(1)
  }
}

object SendInfoToClientActor {
  def props(channel: ChannelHandlerContext) = Props(new SendInfoToClientActor(channel))
}
