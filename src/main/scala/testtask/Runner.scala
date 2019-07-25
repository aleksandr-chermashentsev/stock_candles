package testtask

import java.util.concurrent.Executors

import testtask.actors.MarketDataHolderActor
import akka.actor.ActorSystem
import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.{ChannelInitializer, ChannelOption}
import testtask.network.{TcpClient, TcpServer}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Runs server which waits for test client and client which waits for market data
  *
  **/
object Runner extends App {

  implicit val executorContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  val system = ActorSystem("marketdata-system")
  val marketDataHolderActor = system.actorOf(MarketDataHolderActor.props, "marketData")
  Future({
    new TcpServer(Settings.localServerPort, system).run()
  })
  Future({
    new TcpClient(marketDataHolderActor).run()
  })


}
