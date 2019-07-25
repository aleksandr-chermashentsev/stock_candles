package testtask

import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelInitializer, ChannelOption}
import io.netty.handler.codec.string.StringDecoder

/**
 * Test client which receives candles from server
 *         Date: 25.07.2019
 **/
object TestClient extends App {

  val workerGroup = new NioEventLoopGroup()
  try {
    val b = new Bootstrap()
    b.group(workerGroup)
    b.channel(classOf[NioSocketChannel])
    b.option(ChannelOption.SO_KEEPALIVE, Boolean.box(true))
    b.handler(new ChannelInitializer[NioSocketChannel] {
      override def initChannel(ch: NioSocketChannel): Unit =
        ch.pipeline().addLast(new StringDecoder(), new TestClientHandler())
    })
    val f = b.connect("localhost", Settings.localServerPort).sync()
    f.channel().closeFuture().sync()
  }
  finally {
    workerGroup.shutdownGracefully()
  }


}

class TestClientHandler() extends ChannelInboundHandlerAdapter {
  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = cause.printStackTrace()

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    println(msg)
  }
}
