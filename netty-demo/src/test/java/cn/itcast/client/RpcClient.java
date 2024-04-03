package cn.itcast.client;

import cn.itcast.client.handler.RpcResponseMessageHandler;
import cn.itcast.message.RpcRequestMessage;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    // 第十二步：客户端接收服务端写回来的消息，并对其进行粘包、半包处理
                    ch.pipeline().addLast(new ProcotolFrameDecoder()); // 入站处理器
                    // 第三步：由于 writeAndFlush 方法触发出站操作（自下而上），所以执行该解析器，对消息进行打印
                    // 第十三步：对接收的数据进行打印
                    ch.pipeline().addLast(LOGGING_HANDLER); // 出、入站处理器
                    // 第二步：由于 writeAndFlush 方法触发出站（自下而上）操作，所以执行该解析器，对消息按照一定协议进行编码，在网络中通过字节的方式传输
                    // 第十四步：对接收的字节数据按照协议规则进行解码，变成具体的对象
                    ch.pipeline().addLast(MESSAGE_CODEC);   // 出、入站处理器
                    // 第十五步：对解码后数据做最终处理
                    ch.pipeline().addLast(RPC_HANDLER);     // 入站处理器    --等待消息接收
                }
            });
            // 这段代码本身是由主线程来执行的，但是调用连接方法时，会开辟eventLoop线程来执行，而 sync会阻塞住主线程继续向下运行，直到连接完毕。
            Channel channel = bootstrap.connect("localhost", 9999).sync().channel();
                // 第一步：将调用请求发送给服务端
            RpcRequestMessage sayHello = new RpcRequestMessage(
                    1,
                    "cn.itcast.server.service.HelloService",
                    "sayHello",
                    String.class,
                    new Class[]{String.class},
                    new Object[]{"张三"}
            );
            // 这段代码本身是由主线程来执行的，但是调用写入方法时，会使用eventLoop线程（和上面连接方法用的是同一个）来执行，并且不会阻塞住主线程继续向下运行。
            ChannelFuture future = channel.writeAndFlush(sayHello);
            // 此处为异步线程（eventLoop）的一个回调，和上面的代码是同一个eventLoop
            future.addListener(promise -> {
                if(!promise.isSuccess()){
                    Throwable cause = promise.cause();
                    log.info("error",cause);
                }
            });

//            ChannelFuture future = channel.writeAndFlush(new RpcRequestMessage(
//                    1,
//                    "cn.itcast.server.service.HelloService",
//                    "sayHello",
//                    String.class,
//                    new Class[]{String.class},
//                    new Object[]{"张三"}
//            )).addListener(promise -> {
//                if (!promise.isSuccess()) {
//                    Throwable cause = promise.cause();
//                    log.error("error", cause);
//                }
//            });


            //这段代码不是关闭通道，而是等待通道关闭，当通道关闭 （执行channel.close方法，此方法也是异步的，由eventLoop线程具体执行，然后会给返回的ChannelFuture对象做标记，
            // 而 channel.closeFuture() 方法就是在等待这个 ChannelFuture 出现完成标记，当close方法调用后，ChannelFuture出现标记，则会执行 closeFuture 方法，并向下继续执行代码，否则会移植阻塞
            // ）后，如果通道关闭，则释放资源
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
