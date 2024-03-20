package cn.itcast.protocol;

import cn.itcast.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * ByteToMessageCodec  : 将byteBuf和自定义消息进行转换，类型是：Message（抽象父类）
 *  * 自定义协议要素：
 *  * 1、魔数：用来第一时间判断是否是有效包
 *  * 2、版本号：可以支持协议的升级
 *  * 3、序列化算法：消息正文采用那种序列化方式，例如 json、jdk、protobuf、hessian等
 *  * 4、指令类型：登录、注册、单聊、群聊
 *  * 5、请求序号：为了双工通信，提供异步能力
 *  * 6、正文长度
 *  * 7、消息正文
 *
 */

@Slf4j
public class MessageCodec extends ByteToMessageCodec<Message> {

    // 出站前编码 将消息转换成ByteBuf
    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        // 4字节 约定魔数 （类似于暗号）
        out.writeBytes(new byte[]{1,2,3,4});
        // 1字节 版本
        out.writeByte(1);
        // 1字节 序列化方式  ：比如 0代表jdk 1代表json
        out.writeByte(0);
        // 1字节 字节的指令类型（在Message里）
        out.writeByte(msg.getMessageType());
        // 4个字节 请求序号
        out.writeInt(msg.getSequenceId());
        //由于 4+1+1+1+4+4 =15 java中传输字节通常是偶数，所以添加一个无意义的字节，用于对齐 填充
        out.writeByte(0xff);
        // 获取内容的字节数组(将消息对象转化为字节数组)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        byte[] bytes = bos.toByteArray();
        // 4字节 获取内容长度
        out.writeInt(bytes.length);
        // 写入内容
        out.writeBytes(bytes);



    }

    // 入站前将byteBuf转化成message消息
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 读取魔数 4字节
        int magicNum = in.readInt();
        // 读取版本号 1字节
        byte version = in.readByte();
        // 序列化方式 1字节
        byte serializerType = in.readByte();
        // 指令类型 1字节
        byte messageType = in.readByte();
        // 请求序号 4字节
        int sequenceId = in.readInt();
        // 无意义字节 1字节
        in.readByte();
        int length = in.readInt();
        byte[] bytes = new byte[length];
        //读取消息内容
        ByteBuf byteBuf = in.readBytes(bytes, 0, length);
        //将byte数组反序列化成对象
             ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Message message = (Message) ois.readObject();
        log.info("{},{},{},{},{},{}", magicNum, version,serializerType,messageType,sequenceId, length);
        log.info("{}",message);

        // 解码后的数据 需要放到 list中，否则handler拿不到消息
        out.add(message);


    }
}
