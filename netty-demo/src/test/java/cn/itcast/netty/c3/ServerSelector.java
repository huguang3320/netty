package cn.itcast.netty.c3;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
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
        log.debug("register key:{}",sscKey);
        ssc.bind(new InetSocketAddress(8888));
        while (true){
           //3、select 方法，没有事件发生时，线程阻塞，有事件时，线程才会恢复运行
            // select 在事件未处理时，它不会阻塞，事件发生后，要么取消要么处理，不能置之不理
            // 总结，没事件时阻塞，有事件，但是事件没处理，不会阻塞
            selector.select();
           //4、处理事件，selectedKeys 内部包含了所有发生的事件的key
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()){
                SelectionKey key = iterator.next();
                log.debug("key:{}",key);
//                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
//                SocketChannel accept = channel.accept();
//                log.debug("{}",accept);
                key.cancel();
            }
        }

    }
}
