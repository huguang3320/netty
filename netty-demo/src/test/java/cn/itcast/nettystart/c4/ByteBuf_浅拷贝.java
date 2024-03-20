package cn.itcast.nettystart.c4;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static cn.itcast.nettystart.c4.TestByteBuf_写入方法.log;


/**
 * 使用slice切片 实现数据的拷贝
 * 切片后的buf和原buf属于同一个，如果修改了其中一个字节内容，原buf也会跟着改变
 * 原buf如果释放资源，则切片buf也会被释放，所以需要通过retain值加1来保证切片buf不被释放掉
 */
public class ByteBuf_浅拷贝 {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{'a','b','c','d','e','f','g','h','i','j'});
        log(buf);

        //在切片过程中，没有发生数据复制，切片后会对新的ByteBuf做容量限制，不允许添加
        // 修改切片生成的buf后，原来的buf也会被改变
        ByteBuf f1 = buf.slice(0,5);
        f1.retain();
        // 通过对计数器加1 来防止切片后的buf被释放掉
        ByteBuf f2 = buf.slice(5,5);
        f2.retain();

        // 释放原有的buf
        System.out.println("释放原有buf");
        buf.release();

    }
}
