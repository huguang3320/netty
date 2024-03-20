package cn.itcast.protocol;

import cn.itcast.message.LoginRequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;

/**
 * 1、测试  出站前编码（发消息前编码）
 *         入站后解码（收到消息后解码）
 * 2、测试  如果入站后数据不完整 会出现半包问题，通过帧解码器来解决
 *         通过将buf数据分为两次发送，来模拟半包问题，帧解码器在接收到第一条数据后，会进行等待，接收到第二条消息后，会将两条消息进行拼接，然后继续执行后面的handler
 *         （此代码是转给了MessageCodec的handler，用于将完整的消息进行解码）
 *
 */

public class TestMessageCodec {

    public static void main(String[] args) throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(
                // 添加帧解码器
                //参数1：最大不能超过1024字节
                //参数2：偏移量，从12个字节后开始就是长度字节+内容字节
                //参数3：长度字节个数 4
                //参数4：长度字节需不需要调整 0 -不需要
                //参数5：是否需要去掉非消息内容 0 -不需要
                new LengthFieldBasedFrameDecoder(1024,12,4,0,0),
                new LoggingHandler(),
                new MessageCodec());
        // encode  对消息进行编码，并出站
        LoginRequestMessage loginRequestMessage = new LoginRequestMessage("zhangsan","123");
        channel.writeOutbound(loginRequestMessage);

        //用于模拟消息的出站，从而获取到buf对象
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        new MessageCodec().encode(null,loginRequestMessage,buf);
        // 将原来的buf分为两段 由于slice方法采用的是零拷贝，所以slice1和slice2共用同一个buf
        ByteBuf slice1 = buf.slice(0, 100);
        ByteBuf slice2 = buf.slice(100, buf.readableBytes() - 100);
        // 入站
        // 如果不加帧解码器 会报错 readerIndex(16) + length(198) exceeds writerIndex(100): 含义是我在16个字节的基础上，应该再读取198个字节，可实际上我只读取了100个字节，所以报错
        // 如果加解码器，只读取一部分后，会继续等待，所以帧解码器后面的handler不会执行
        slice1.retain();        //使引用计数增长为1，如果不执行这段代码，在channel.writeInbound(slice1);执行后会release掉buf资源，导致buf消失，所以需要引用计数加1，来保活buf
        channel.writeInbound(slice1);
        channel.writeInbound(slice2);
    }

}
