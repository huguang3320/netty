package cn.itcast.netty.c1;

import java.nio.ByteBuffer;

public class TestByteBufferReadWrite {

    public static void main(String[] args) {
        //创建一个10个字节空间的缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(10);
        //写入一个字节 'a'
        buffer.put((byte) 0x61);        //写
        ByteBufferUtil.debugAll(buffer);
        //再写入几个字节
        buffer.put(new byte[] {0x62});
        ByteBufferUtil.debugAll(buffer);
        //切换为读模式(不切换读模式，则不会回到初始位置进行读取，而是写到哪个位置，就从写的位置的下一个位置开始读)
        buffer.flip();
        System.out.println(buffer.get());

        //切换为写模式
        //buffer.clear();   //使用这种写模式，索引会回到0，将新内容从索引0的位置开始写入，不管哪种写的方式，索引最后停留的位置上可能都有假数据，而在下次写的时候会覆盖掉这个假数据
        buffer.compact();        //使用这种写模式，索引不从0开始，而是从将上一次的数据向前堆积，然后从堆积后的位置开始继续写，这样能够保证不丢数据
        ByteBufferUtil.debugAll(buffer);
        buffer.put(new byte[] {0x63});
        ByteBufferUtil.debugAll(buffer);
    }
}
