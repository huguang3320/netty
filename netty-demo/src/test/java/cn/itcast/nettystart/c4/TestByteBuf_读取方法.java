package cn.itcast.nettystart.c4;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * 详解 ByteBuf 读取的方法
 *  使用 buf.markReaderIndex()方法对当前读指针的索引位置做标记
 *  使用 buf.resetReaderIndex()方法将当前指针还原到打标记的位置
 *  还有一些列以get开头的方法也可以实现
 *
 */

public class TestByteBuf_读取方法 {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        buf.writeBytes(new byte[]{1,2,3,4});
        buf.writeInt(5);
        buf.writeInt(6);    //当前读指针的索引是位置是 0
        System.out.println(buf.readByte());
        System.out.println(buf.readByte());
        System.out.println(buf.readByte());
        System.out.println(buf.readByte()); // 当前读指针索引位置是 4
        //如果此时想重复读取 5 ，则需要在当前指针位置打标记
        buf.markReaderIndex();
        System.out.println(buf.readInt());  // 读取00 00 00 05
        // 如果此时想重复读取 5 的话，则可以用reset方法
        buf.resetReaderIndex();
        System.out.println(buf.readInt());  // 读取00 00 00 05


        log(buf);
     }

    // 打印16进制数
    private static void log(ByteBuf buffer){
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
