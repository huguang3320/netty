package cn.itcast.nettystart.c2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

/**
 * 构建一个netty的客户端，用于发送数据
 * 详细解读向服务端发送数据时，channel的执行过程
 */
public class EventLoopClient2 {
    public static void main(String[] args) throws InterruptedException {
        // 带有 Future、Promise的类型都是和异步方法进行使用
        ChannelFuture channelFuture = new Bootstrap()
                //2、添加EventLoop
                .group(new NioEventLoopGroup())
                //3、选择客户端 channel 事件
                .channel(NioSocketChannel.class)
                //4、添加处理器，
                .handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override   //在连接建立后就会执行
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        //增加编码器
                        ch.pipeline().addLast(new StringEncoder()); //  new StringEncoder() 这个方法在发送数据后才会执行，把"helloworld"转化成bytebuf

                    }
                })
                //5、连接到服务器   connect是异步非阻塞，主线程 调用该方法时会安排其他线程（nio线程）去连接，主线程继续执行后面的代码
                .connect(new InetSocketAddress("localhost", 9999));     //建立连接比较慢，大概需要1s

                /**
                 * 使用方法 sync 来同步完成线程的阻塞和释放（后续代码还是由主线程进行执行）
                 */
//                channelFuture.sync();   //发起调用的线程（主线程） 变成阻塞状态，直到nio线程建立连接完毕，主线程才会继续执行
//                Channel channel = channelFuture.channel();  // 无阻塞的情况下获取到的channel有可能还未建立连接，导致channel是空的
//                channel.writeAndFlush("hello,world");//发送数据

                /**
                 * 使用方法 addListener(回调对象) 方法 异步处理结果 (彻底交给nio来执行)
                 */
                channelFuture.addListener(new ChannelFutureListener() {
                    @Override   //在nio线程建立好连接后，会调用 operationComplete 方法
                    public void operationComplete(ChannelFuture future) throws Exception {
                        // 此处的channel是nio线程来负责创建和管理的，已经不是由主线程创建的了
                        Channel channel = future.channel();
                        channel.writeAndFlush("hello,world");
                    }
                });
    }
}
