package cn.itcast.nettystart.c2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;

/**
 * 构建一个netty的客户端，用于发送数据
 * 用聊天的方式模拟关闭 channelFuture
 */
@Slf4j
public class EventLoopClient3 {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
        // 带有 Future、Promise的类型都是和异步方法进行使用
        ChannelFuture channelFuture = new Bootstrap()
                //2、添加EventLoop
                .group(nioEventLoopGroup)
                //3、选择客户端 channel 事件
                .channel(NioSocketChannel.class)
                //4、添加处理器，
                .handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override   //在连接建立后就会执行
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        //增加编码器
                        ch.pipeline().addLast(new StringEncoder()); //  new StringEncoder() 这个方法在发送数据后才会执行，把"helloworld"转化成bytebuf
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                    }
                })
                //5、连接到服务器   connect是异步非阻塞，主线程 调用该方法时会安排其他线程（nio线程）去连接，主线程继续执行后面的代码
                .connect(new InetSocketAddress("localhost", 9999));     //建立连接比较慢，大概需要1s
                Channel channel = channelFuture.sync().channel();
                log.info("channel:{}",channel);
                new Thread(() -> {
                    Scanner scanner = new Scanner(System.in);
                    while(true){
                        String line = scanner.nextLine();
                        if("q".equals(line)){
                            // 关闭channel 但是.close方法也是异步的，并不是主线程去关闭，而是交给另一个线程进行关闭
                            channel.close();
                            break;
                        }
                        channel.writeAndFlush(line);
                    }
                },"input").start();

                // 获取CloseFuture对象， 1）同步处理关闭（主线程发起等待，等待channel.close执行完毕后，继续执行主线程代码）
                // 2) 异步处理关闭 (由nio线程进行等待，当nio分配的线程执行任务后，由nio线程执行后续代码)
        ChannelFuture closeFuture = channel.closeFuture();
//        //等待线程执行后，进行关闭..
//        log.info("等待子线程关闭...");
//        closeFuture.sync();
//        log.info("子线程已处理完任务并关闭channel，继续执行主线程代码");

        // nio线程的回调函数——当 channel.close执行结束后，执行改代码
        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                log.info("子线程已处理完任务并关闭channel，继续执行nio代码");
                //所有任务都执行完毕，关闭niogroup中的所有线程
                nioEventLoopGroup.shutdownGracefully();
            }
        });




    }
}
