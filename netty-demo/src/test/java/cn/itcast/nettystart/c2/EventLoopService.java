package cn.itcast.nettystart.c2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * 构建一个netty的服务端，模拟使用EventLoop发送、接收IO请求，创建两个 NioEventLoopGroup ，其中一个只负责管理建立连接，另一个NioEventLoopGroup开了两个线程，负责处理接收的事件
 */
@Slf4j
public class EventLoopService {
    public static void main(String[] args) {
        new ServerBootstrap()
                // 此处可以创建多个 NioEventLoopGroup ，作为分工，boss只用于处理建立连接事件，worker只用于处理接收事件
                .group(new NioEventLoopGroup(),new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                ch.pipeline().addLast(new StringDecoder());

                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){   // 自定义的handler
                                    @Override   //channelRead：处理读事件 ，处理其他事件可以用其他的方法
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        log.info("{}",msg);
                                    }
                                });
                            }
                        }
                )
                .bind(9999);
    }


}
