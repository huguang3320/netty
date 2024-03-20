package cn.itcast.nettystart;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 构建一个netty的服务端，用于接收客户端发来的数据
 */
public class HelloServer {
    public static void main(String[] args) {
        // serverBootstrap ：服务器端的启动器类，用于组装 netty 组件
        new ServerBootstrap()
                // NioEventLoopGroup 中包含选择器（selector）和线程（thread），用于管理 EventLoop 的线程池，是一组 EventLoop 的集合，每个 EventLoop 都负责处理一个或多个 Channel 的 I/O 事件。
                // 例如：一个eventLoop 可以接收 ACCEPT事件，另一个eventLoop可以接收READ事件，在将接收到的数据
                .group(new NioEventLoopGroup())
                //选择服务器的 ServerSocketChannel 来实现 ，ServerSocketChannel 是 Netty 中用于监听和接受客户端连接请求的通道
                .channel(NioServerSocketChannel.class)
                // boss 负责连接，worker（child）负责处理读写，childHandler决定了worker(child) 能执行哪些操作 (handler)
                .childHandler(
                        // 代表和客户端进行数据读写的初始化工作，一旦服务端与客户端建立连接，就会执行里面的方法
                        new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) throws Exception {
                                // 添加具体的处理内容 handler
                                // 添加解码，将接收到的ByteBuf(存储字节)数据转化成字符串数据
                                ch.pipeline().addLast(new StringDecoder());
                                // 接收客户端发来的数据
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){   // 自定义的handler
                                    @Override   //channelRead：处理读事件 ，处理其他事件可以用其他的方法
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        System.out.println(msg);
                                    }
                                });
                            }
                        }
                )
                .bind(9999);
    }
}
