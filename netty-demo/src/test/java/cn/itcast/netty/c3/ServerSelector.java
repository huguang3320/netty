package cn.itcast.netty.c3;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static cn.itcast.netty.c1.ByteBufferUtil.debugRead;

@Slf4j
public class ServerSelector {
    public static void main(String[] args) throws IOException {
        // 1、创建 selector 管理多个channel
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); // 非阻塞模式

        // 2、建立selector 和 channel 的联系（将channel 注册到selector中）
        // SelectionKey 就是将来事件发生后，通过它可以知道事件和哪个channel的事件
        SelectionKey sscKey = ssc.register(selector,0,null);
        //key 只关注accept事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);
        log.debug("注册的 key 值:{}",sscKey);
        ssc.bind(new InetSocketAddress(8888));
        while (true){
           //3、select 方法，没有事件发生时，线程阻塞，有事件时，线程才会恢复运行
            // select 在事件未处理时，它不会阻塞，事件发生后，要么取消要么处理，不能置之不理
            // 总结，没事件时阻塞，有事件，但是事件没处理，不会阻塞
            // **** selector.select 将所有注册到selector中的通道的key保存，形成集合,如果又有新的连接，直接从此处开始执行代码
            selector.select();
           //4、处理事件，selectedKeys 内部包含了所有发生的事件的key
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                //集合中的key值
                SelectionKey key = iterator.next();
                //处理key时，要从selectKeys集合中删除，否则下次处理会有问题
                iterator.remove();
                log.debug("key值:{}",key);
                //5、区分事件类型
                if(key.isAcceptable()){
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel accept = channel.accept();
                    accept.configureBlocking(false);
                    SelectionKey scKey = accept.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}",accept);
                } else if (key.isReadable()){           //如果是 read
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(4);
                        int read = channel.read(buffer);//如果不做任何处理，此时如果连接断开，程序会在此处报错，并且抛出异常，服务被强迫停止
                        if(read == -1 ){    //当客户端断开连接时channel.read = -1
                            key.channel();
                        } else {
                            buffer.flip();
                            //debugRead(buffer);
                            System.out.println(Charset.defaultCharset().decode(buffer));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();        //如果使用try catch抓住异常，程序会继续进行，服务不会被强迫停止
                        key.cancel();  // 因为客户端断开了，因此需要将key取消 （从 selector 的 keys 集合中真正的删除）
                    }
                }

               // key.cancel();
            }
        }

    }
}
