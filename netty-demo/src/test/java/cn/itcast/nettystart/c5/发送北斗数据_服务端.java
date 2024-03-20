package cn.itcast.nettystart.c5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Random;

/**
 * 粘包、半包问题的服务端代码
 */
public class 发送北斗数据_服务端 {



    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            //调整系统的接收缓冲区（滑动窗口）
            //serverBootstrap.option(ChannelOption.SO_RCVBUF,10);
            //调整netty的接收缓冲区（ByteBuf空间）
            serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(16,16,16));
            serverBootstrap.group(boss,worker);

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {


                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            ByteBuf buf = ctx.alloc().buffer();
                            String s = "$CCTCQ,4205116,2,1,3,A44447474430304132303030344432303130394645463430314338303230453031303830463945413731363245463432343041333039303030334645463330303135414331313238353442343246333332393234314638353035413031354131313031363430303343303035354646463630353030303030304535334630304438303137303031333530303939304342353237393730323646304130353036303231414137303345383036\n,0*3D";
                            buf.writeBytes(s.getBytes());
                        }
                    });
                }
            });
            ChannelFuture channelFuture = serverBootstrap.bind(9999).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
