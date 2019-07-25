package testtask.network

import java.util.concurrent.Executors

import akka.actor.{ActorSystem, PoisonPill}
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.string.StringEncoder
import testtask.Settings
import testtask.actors.Messages.{Start, Tick}
import testtask.actors.SendInfoToClientActor

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps


/**
  * Server which waits for tcp connections. When connection occurs, sends candles to new client
  * and subscribes him on new candles
  * Date: 25.07.2019
  **/
class TcpServer(val port: Int, val actorSystem: ActorSystem) {

  implicit val executorContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))

  def run() = {
    val workerGroup = new NioEventLoopGroup
    val bossGroup = new NioEventLoopGroup

    try {
      val b = new ServerBootstrap
      b.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .childHandler(new ChannelInitializer[SocketChannel] {
          override def initChannel(ch: SocketChannel): Unit =
            ch.pipeline().addLast(new StringEncoder(), new TcpServerHandler(actorSystem))
        })
        .childOption(ChannelOption.SO_KEEPALIVE, Boolean.box(true))

      val chFuture = b.bind(port).sync()
      chFuture.channel().closeFuture().sync()
    } finally {
      workerGroup.shutdownGracefully()
      bossGroup.shutdownGracefully()
    }
  }

  class TcpServerHandler(val actorSystem: ActorSystem) extends ChannelInboundHandlerAdapter {


    override def channelActive(ctx: ChannelHandlerContext): Unit = {
      println(s"Context active ${ctx.channel().id()}")
      val sendInfoActor = actorSystem.actorOf(SendInfoToClientActor.props(ctx), s"sendInfoToClient${ctx.channel().id()}")
      sendInfoActor ! Start
      actorSystem.scheduler.schedule(Settings.candlesTimeInterval, Settings.candlesTimeInterval, sendInfoActor, Tick)
    }

    override def channelInactive(ctx: ChannelHandlerContext): Unit = {

      println(s"Context inactive ${ctx.name}")
      actorSystem.actorSelection(s"/user/sendInfoToClient${ctx.channel().id()}") ! PoisonPill
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = cause.printStackTrace()

  }

}
