package cn.itcast.server;

import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import cn.itcast.server.handler.RpcRequestMessageHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        RpcRequestMessageHandler RPC_HANDLER = new RpcRequestMessageHandler();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 第四步：接收客户端发来的消息，对其进行粘包、半包处理
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    // 第五步：对接到的消息进行打印
                    // 第十一步：出站前将消息进行打印
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    // 第六步：对消息按协议规则进行解码，并转成具体的消息对象
                    // 第十步：出站前将消息对象转化为一定协议的字节数据，传输给客户端
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    // 第七步：执行RPC方法，根据消息对象进行解析 获取返回值结果
                    ch.pipeline().addLast(RPC_HANDLER);
                }
            });
            Channel channel = serverBootstrap.bind(9999).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
