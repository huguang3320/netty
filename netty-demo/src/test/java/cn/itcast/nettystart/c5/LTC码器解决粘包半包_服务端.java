package cn.itcast.nettystart.c5;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * LTC解码器解决粘包、半包问题
 * 原理是 发送消息时需要发送消息的长度，API会根据这个长度读取对应的消息进行拆分
 */
public class LTC码器解决粘包半包_服务端 {



    public static void main(String[] args) {
        // 模拟服务端
        EmbeddedChannel embeddedChannel = new EmbeddedChannel(
                // 增加两个处理器 LengthFieldBasedFrameDecoder 就是LTC解码器
                // 参数1：消息的最大长度
                // 参数2：长度的偏移量（就是第几个字节是长度，用于定位长度的位置）
                // 参数3：实际长度的字节数 因为本例是写了一个int类型的长度，所以长度字节应该是4
                // 参数4：读取长度后，需要调整几个字节数，不需要调整就写0
                // 参数5：解析的结果需不需要剥离，需要剥离几个字节，比如 长度是4个字节，需要把这4个字节去除掉，那这里就填写4
                new LengthFieldBasedFrameDecoder(1024,0,4,0,4),
                new LoggingHandler(LogLevel.DEBUG)
        );

        // 发送4个字节的内容长度， 然后是实际内容
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        send(buffer,"Hello, world");
        send(buffer,"nh, dgg");
        send(buffer,"ws, dtt");
        embeddedChannel.writeInbound(buffer);
    }

    private static void send(ByteBuf buffer, String content){
        byte[] bytes = content.getBytes();
        int length = bytes.length;
        // 将长度写入到4个字节里
        buffer.writeInt(length);
        // 写入消息
        buffer.writeBytes(bytes);
    }

}
