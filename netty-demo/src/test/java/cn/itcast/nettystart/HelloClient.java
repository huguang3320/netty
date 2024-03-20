package cn.itcast.nettystart;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

/**
 * 构建一个netty的客户端，用于发送数据
 */
public class HelloClient {
    public static void main(String[] args) throws InterruptedException {
        new Bootstrap()
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
                //5、连接到服务器
                .connect(new InetSocketAddress("localhost",9999))
                .sync()     //阻塞方法，直到连接建立，才会继续执行后面的代码
                .channel()  //连接成功后 获取连接对象（socketchannel对象）
                //向服务器发送请求
                .writeAndFlush("hello,world");  //发送数据

    }
}
