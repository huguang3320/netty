package cn.itcast.nettystart.c4;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * 详解 ByteBuf 写入的方法
 *
 * int 占4个字节
 * short 占2个字节
 * long 占8个字节
 * byte 占1个字节
 * float 占4个字节
 * double 占8个字节
 * char 占2个字节
 * boolean 占1个字节
 *
 * 扩容规则：
 *      如果写入后字节大小未超过512个，则选择下一个16的整数倍，例如写入后字节大小是12，初始大小是10，需要扩容，则扩容后的大小是 16
 *      如果写入后字节大小超过512个，则选择下一个2^n,例如写入后大小是513，则扩容后是 2^10 = 1024 (2^9 已经不够了)
 *
 *
 */

public class TestByteBuf_写入方法 {
    public static void main(String[] args) {
        // "abc"占3个字节 ， "你好啊" 占9个字节
        //System.out.println("abc".getBytes().length);

        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        // 大端写入0x250(16进制数),按正常顺序存入buf中，  得到的结果是   00 00 02 50
        // 0x250 转化成二进制应该是 0010 0101 0000 ，由于int类型占4个字节，每个字节有8位，所以转化成二进制数按大端字节序存储（高位在前，地位在后）应该是：
        // 0000 0000 0000 0000  0000 0010 0101 0000
        // 由于每4位2进制数代表1个十六进制数，所以转化成16进制应该是 00 00 02 50
        //buf.writeInt(0x250);
        //log(buf);
        // 小端写入0x250(16进制数),按正常顺序存入buf中，  得到的结果是   50 02 00 00
        // 0x250 转化成二进制应该是 0010 0101 0000 ，由于int类型占4个字节，每个字节有8位，所以转化成二进制数按小端字节序存储（高位在后，低位在前，这里的低位字节应该是 0101 0000，而不是 0000 0101，因为是按字节排序(1字节8位)，而不是按4个位排序 ）应该是：
        // 0101 0000 0000 0010 0000 0000 0000 0000
        // 由于每4位2进制数代表1个十六进制数，所以转化成16进制应该是 50 02 00 00
        //buf.writeIntLE(0x250);


        // 写入4个字节  每个字节是一个10进制数，对应16进制分别是 01 02 03 04 ，所以打印出来的内容是 01 02 03 04
        buf.writeBytes(new byte[]{1,2,3,4});

        // 再写一个int整数， 5是一个10进制数，对应16进制是 05 ,所以打印出来的内容是 05
        buf.writeInt(5);

        buf.writeInt(10);
        log(buf);
     }

    // 打印16进制数
    public static void log(ByteBuf buffer){
        int length = buffer.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf = new StringBuilder(rows * 88 * 2 )
                .append("read index:").append(buffer.readerIndex())
                .append(" write index:").append(buffer.writerIndex())
                .append(" capacity:").append(buffer.capacity())
                .append(NEWLINE);
        appendPrettyHexDump(buf, buffer);
        System.out.println(buf.toString());

    }
}
