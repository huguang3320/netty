package cn.itcast.test;

import cn.itcast.config.Config;
import cn.itcast.message.LoginRequestMessage;
import cn.itcast.message.Message;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 测试序列化的两种方式  jdk json
 */

public class 测试序列化与反序列化 {
    public static void main(String[] args) {
        MessageCodecSharable CODEC = new MessageCodecSharable();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler();
        //打印两次的含义在于检测出、入站前获取的数据是什么，经过编码后，出、入站之后的消息是什么
        EmbeddedChannel channel = new EmbeddedChannel(LOGGING_HANDLER, CODEC, LOGGING_HANDLER);
        LoginRequestMessage message = new LoginRequestMessage("zhangsan", "123");
        //模拟写入消息后 出站
        //channel.writeOutbound(message);
        //模拟写入消息后返回的字节对象
        ByteBuf byteBuf = messageToByteBuf(message);
        // 模拟入站 接收消息
        channel.writeInbound(byteBuf);
    }

    // 用于测试反序列化时模拟的出站编码
    public static ByteBuf messageToByteBuf(Message msg) {
        int algorithm = Config.getSerializerAlgorithm().ordinal();
        ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
        out.writeBytes(new byte[]{1, 2, 3, 4});
        out.writeByte(1);
        out.writeByte(algorithm);
        out.writeByte(msg.getMessageType());
        out.writeInt(msg.getSequenceId());
        out.writeByte(0xff);
        byte[] bytes = Serializer.Algorithm.values()[algorithm].serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
        return out;
}
}