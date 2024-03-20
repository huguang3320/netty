package cn.itcast.nettystart.c2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * 构建一个netty的服务端，模拟使用EventLoop发送、接收IO请求
 * 另外有一种情况是：一个事件可以交给两个不同的 EventLoop 进行处理，可将复杂的任务交给单独一个eventLoop进行处理，从而节省某个线程的占用
 */
@Slf4j
public class EventLoopService2 {
    public static void main(String[] args) {
       EventLoopGroup group = new DefaultEventLoop();
        new ServerBootstrap()
                // 此处可以创建多个 NioEventLoopGroup ，作为分工，boss只用于处理建立连接事件，worker只用于处理接收事件
                .group(new NioEventLoopGroup(),new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                ch.pipeline().addLast("handler1",new ChannelInboundHandlerAdapter(){   // 自定义的handler
                                    @Override   //channelRead：处理读事件 ，处理其他事件可以用其他的方法
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        ByteBuf buf = (ByteBuf) msg;
                                        log.info(buf.toString(Charset.defaultCharset()));   //让消息传递给下一个 handler
                                    }
                                }).addLast(group,"handler2",new ChannelInboundHandlerAdapter(){     //group 是另个一个eventLoop
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        ByteBuf buf = (ByteBuf) msg;
                                        log.info(buf.toString(Charset.defaultCharset()));
                                    }
                                })
                                ;
                            }
                        }
                )
                .bind(9999);
    }
}
