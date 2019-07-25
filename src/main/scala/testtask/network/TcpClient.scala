package testtask.network

import java.nio.charset.StandardCharsets

import akka.actor.{ActorRef, ActorSystem}
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import testtask.Settings
import testtask.actors.MarketDataHolderActor
import testtask.actors.Messages.MarketData

/**
  * Receives market data and decode it
  */
class TcpClient(marketDataHolderActor: ActorRef) {

  def run(): ChannelFuture = {
    val workerGroup = new NioEventLoopGroup()

    try {
      val b = new Bootstrap()
      b.group(workerGroup)
      b.channel(classOf[NioSocketChannel])
      b.option(ChannelOption.SO_KEEPALIVE, Boolean.box(true))
      b.handler(new ChannelInitializer[NioSocketChannel] {
        override def initChannel(ch: NioSocketChannel): Unit =
          ch.pipeline().addLast(new MarketDataHandler(marketDataHolderActor))
      })
      val f = b.connect("localhost", Settings.dataSourcePort).sync()
      f.channel().closeFuture().sync()
    }
    finally {
      workerGroup.shutdownGracefully()
    }
  }

}

class MarketDataHandler(val marketDataHolderActor: ActorRef) extends ChannelInboundHandlerAdapter {

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = cause.printStackTrace()

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    val data = msg.asInstanceOf[ByteBuf]
    //skip length field because messages fit in one frame
    val timestamp = data.getLong(2)
    val tickerLength = data.getShort(10)
    val tickerBytes = new Array[Byte](tickerLength)
    data.getBytes(12, tickerBytes)
    val price = data.getDouble(12 + tickerLength)
    val size = data.getInt(20 + tickerLength)

    val marketData = MarketData(timestamp, new String(tickerBytes, StandardCharsets.US_ASCII), price, size)
    marketDataHolderActor ! marketData
  }
}
