package cn.itcast.nettystart.c4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static io.netty.buffer.ByteBufUtil.appendPrettyHexDump;
import static io.netty.util.internal.StringUtil.NEWLINE;

/**
 * 展示 netty 中的 ByteBuf的自动扩容，ByteBuffer 设定好容量后，写入超过预定值后就会报错，而ByteBuf会自动扩容
 *
 * 讲解 ByteBuf 是存放在堆内存还是直接内存中：
 * 存放在堆内存：分配效率高，但是读写效率低（堆内存会受到垃圾回收的影响，把对象进行搬迁、复制，会影响到效率）
 * 直接内存：分配效率低，读写效率高（系统内存会直接映射到java中的内存，减少了一次内存复制）
 * 所以netty默认使用直接内存来作为ByteBuf的内存
 *
 * 池化的基于堆内存的ByteBuf创建：
 *      ByteBufAllocator.DEFAULT.heapBuffer(10)
 * 池化的基于堆内存的ByteBuf创建：
 *      ByteBufAllocator.DEFAULT.Buffer(10)/ByteBufAllocator.DEFAULT.directBuffer(10)
 *
 * 池化管理：（对于创建比较慢的资源，用池的思想来优化，类似于mysql连接池，预先创建出昂贵的对象，使用时省去创建步骤，用完后归还）
 *      池化的最大意义是可以重用 ByteBuf
 *
 * ByteBuf 包含四部分：读指针，写指针，容量，最大容量
 *
 */
public class TestByteBuf {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        log(buf);
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            // 一个字符 占用 一个字节
            s.append("a");
        }
        buf.writeBytes(s.toString().getBytes());
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
