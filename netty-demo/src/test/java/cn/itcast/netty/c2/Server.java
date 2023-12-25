package cn.itcast.netty.c2;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static cn.itcast.netty.c1.ByteBufferUtil.debugRead;

@Slf4j
public class Server {
    public static void main(String[] args) throws IOException {
        //0、生成16字节的buffer缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(16);
        //1、创建了服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); // 非阻塞模式
        //2、绑定监听端口
        ssc.bind(new InetSocketAddress(8888));

        //3、连接的集合
        List<SocketChannel> channels = new ArrayList<>();
        while (true){
            // 4、accpet 建立与客户端连接，SocketChannel 用来与客户端之间通信
            log.debug("connecting....");
            SocketChannel sc = ssc.accept();
            log.debug("connecting....{}",sc);
            channels.add(sc);
            for (SocketChannel channel: channels) {
                // 5、接收客户端发送的数据
                log.debug("connecting....{}",channel);
                channel.read(buffer);
                buffer.flip();
                debugRead(buffer);
                buffer.clear();
                log.debug("connecting....{}",channel);
            }
        }

    }
}
