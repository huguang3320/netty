package cn.itcast.nettystart.c6;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.nio.charset.Charset;

/**
 * 编写与redis通信的协议
 */
public class Rdis协议通信 {
    /**
     * set key value
     * set name zhangsan
     * *3       --代表数组内元素的个数  set key value  是三个元素 所以用 *3表示
     * $3       --set 占3个字节，所以可以用$3表示
     * set
     * $4       --name 占4个字节，所以用$4表示
     * name
     * $8       --zhangsan 占8个字节，所以用$8表示
     */
    public static void main(String[] args) {
        // 定义回车和换行
        final byte[] LINE = {13,10};
        NioEventLoopGroup worker = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(worker);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                // 向redis写入数据
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                    @Override
                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                        ByteBuf buf = ctx.alloc().buffer();
                        buf.writeBytes("*3".getBytes());
                        buf.writeBytes(LINE);
                        buf.writeBytes("$3".getBytes());
                        buf.writeBytes(LINE);
                        buf.writeBytes("set".getBytes());
                        buf.writeBytes(LINE);
                        buf.writeBytes("$4".getBytes());
                        buf.writeBytes(LINE);
                        buf.writeBytes("name".getBytes());
                        buf.writeBytes(LINE);
                        buf.writeBytes("$8".getBytes());
                        buf.writeBytes(LINE);
                        buf.writeBytes("zhangsan".getBytes());
                        buf.writeBytes(LINE);
                        ctx.writeAndFlush(buf);
                    }

                    //从redis中读取数据
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        ByteBuf buf = (ByteBuf) msg;
                        System.out.println(buf.toString(Charset.defaultCharset()));
                    }

                });
            }
        });

        ChannelFuture channelFuture = bootstrap.connect("localhost", 6379);
        channelFuture.channel().closeFuture();

    }
}
