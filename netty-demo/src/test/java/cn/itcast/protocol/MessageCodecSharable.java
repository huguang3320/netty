package cn.itcast.protocol;

import cn.itcast.config.Config;
import cn.itcast.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

@Slf4j
@ChannelHandler.Sharable
/**
 * 必须和 LengthFieldBasedFrameDecoder 一起使用，确保接到的 ByteBuf 消息是完整的
 *
 * // 入站是使用 encode方法，将消息转发为符合协议规则的byte数组入站
 * // 出站调用 decode方法，将消息转发为符合协议规则消息对象出站
 *
 */
public class MessageCodecSharable extends MessageToMessageCodec<ByteBuf, Message> {
    @Override
    public void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        // 1. 4 字节的魔数
        out.writeBytes(new byte[]{1, 2, 3, 4});
        // 2. 1 字节的版本,
        out.writeByte(1);
        // 3. 1 字节的序列化方式 jdk 0 , json 1
        // out.writeByte(0);
        // 3. 1 字节的序列化方式，Config.getSerializerAlgorithm()返回的是具体的枚举对象，ordinal方法则获取值对应的索引
        out.writeByte(Config.getSerializerAlgorithm().ordinal());
        // 4. 1 字节的指令类型
        out.writeByte(msg.getMessageType());
        // 5. 4 个字节
        out.writeInt(msg.getSequenceId());
        // 无意义，对齐填充
        out.writeByte(0xff);
        // 6. 获取内容的字节数组
        // 获取内容的字节数组(将消息对象转化为字节数组)  ——传统方式
        //ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //ObjectOutputStream oos = new ObjectOutputStream(bos);
        //oos.writeObject(msg);
        //byte[] bytes = bos.toByteArray();
        // 第二种方式获取字节数组，通过枚举类中的Java实例 获取对应的序列化方法，但是此方式将示例对象写死了
        //byte[] bytes = Serializer.Algorithm.Java.serialize(msg);
        // 第三种方式获取字节数组，通过枚举类中的Java实例 获取对应的序列化方法，此方式将示例对象写在了配置文件中
        byte[] bytes = Config.getSerializerAlgorithm().serialize(msg);
        // 7. 长度
        out.writeInt(bytes.length);
        // 8. 写入内容
        out.writeBytes(bytes);
        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //魔数
        int magicNum = in.readInt();
        //版本
        byte version = in.readByte();
        //序列化类型
        byte serializerAlgorithm = in.readByte();   // 0 或 1
        // 消息类型
        byte messageType = in.readByte();   // 0,1,2...
        int sequenceId = in.readInt();
        in.readByte();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        // 将读取到的字节数据存入到bytes中
        in.readBytes(bytes, 0, length);
        // 将字节数组转化为message对象
        //ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        //Message message = (Message) ois.readObject();
        // 第二种方式将字节数组转化为message对象 ，通过自定义的反序列化方法实现将字节数组转化为message对象
        //Message message = Serializer.Algorithm.Java.deserialize(Message.class, bytes);
        // 第三种方式将字节数组转化为message对象 ，通过配置的方式调用反序列化方法实现将字节数组转化为message对象
        // 枚举类的values方法是获取所有的枚举实例数组，根据索引值来获取具体哪一个枚举实例，再调用反序列化方法
        Serializer.Algorithm algorithm = Serializer.Algorithm.values()[serializerAlgorithm];
        // 确定消息类型
        Class<?> messageClass = Message.getMessageClass(messageType);
        Object deserialize = algorithm.deserialize(messageClass, bytes);
        //log.debug("{}, {}, {}, {}, {}, {}", magicNum, version, serializerType, messageType, sequenceId, length);
        //log.debug("{}", message);
        out.add(deserialize);
    }

}
