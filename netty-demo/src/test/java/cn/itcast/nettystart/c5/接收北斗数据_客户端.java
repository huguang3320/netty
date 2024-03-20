package cn.itcast.nettystart.c5;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
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
public class 接收北斗数据_客户端 {
    private static void send(ByteBuf buffer, String content){
        byte[] bytes = content.getBytes();
        int length = bytes.length;
        // 将长度写入到4个字节里
        buffer.writeInt(length);
        // 写入消息
        buffer.writeBytes(bytes);
    }

    public static void main(String[] args) throws InterruptedException {
        // 模拟服务端
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                new LengthFieldBasedFrameDecoder(1024,0,4,0,0),
                //new LineBasedFrameDecoder(1024),
                new LoggingHandler(LogLevel.DEBUG)
        );

        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        String s = "$CCTCQ,4205116,2,1,3,A44447474430304132303030344432303130394645463430314338303230453031303830463945413731363245463432343041333039303030334645463330303135414331313238353442343246333332393234314638353035413031354131313031363430303343303035354646463630353030303030304535334630304438303137303031333530303939304342353237393730323646304130353036303231414137303345383036\n,0*3D";
        send(buffer,s);
        embeddedChannel.writeInbound(buffer);


    }
}
