package cn.itcast.netty.c1;


import java.nio.ByteBuffer;

public class TestByteBufferString {

    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        // 字符串转为 ByteBuffer
        buffer.put("hello".getBytes());
        ByteBufferUtil.debugAll(buffer);    //打印出来的68 65 6c ... 都是16进制数，对应的字符参照ASCII码表

        //wrap
        ByteBuffer buffer2 = ByteBuffer.wrap("hello".getBytes());   //自动切换为读模式，索引位置归0
        ByteBufferUtil.debugAll(buffer2);
    }



}
