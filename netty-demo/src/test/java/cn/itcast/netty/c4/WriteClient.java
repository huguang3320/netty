package cn.itcast.netty.c4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class WriteClient {

    public static void main(String[] args) throws IOException {
        SocketChannel ssc = SocketChannel.open();
        ssc.connect(new InetSocketAddress(8080));
        // 接收数据
        int count = 0;
        while (true){
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            count += ssc.read(buffer);
            System.out.println(count);
            buffer.clear();     //buffer清空，指针指向索引0
        }
    }
}
