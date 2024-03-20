package cn.itcast.nettystart.c5;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * 使用行解码器解决粘包、半包代码
 * 行解码器类似于定义好\n  \r\n这种，遇到这种就认为是一条完整数据
 * 还可以自己定义分隔符，DelimiterBasedFrameDecoder
 */

@Slf4j
public class 接收北斗数据2_客户端 {
    private static void send(){
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);  //配置通道
            bootstrap.group(worker);    // 配置组
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {



                //初始化
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LineBasedFrameDecoder(2048));
                    ch.pipeline().addLast(new StringDecoder());
                    //添加信息的加工条件
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.info("========msg:{}",msg);
                        }
                    });

                }
            });

            ChannelFuture channelFuture = bootstrap.connect("10.10.18.41", 4001).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("client error :{}",e);
        } finally {
            worker.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        send();
    }
}
