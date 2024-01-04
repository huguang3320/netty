package cn.itcast.netty.c3;

import cn.itcast.netty.c1.ByteBufferUtil;
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
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    SelectionKey scKey = accept.register(selector, 0, buffer);  // 第三个参数buffer相当于附件，每一个channel对应一个buffer，有10个socketchannel，就有10个buffer附件
                    scKey.interestOps(SelectionKey.OP_READ);
                    log.debug("{}",accept);
                } else if (key.isReadable()){           //如果是 read
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        //获取 selectionKey 上附件关联的buffer
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int read = channel.read(buffer);//如果不做任何处理，此时如果连接断开，程序会在此处报错，并且抛出异常，服务被强迫停止
                        if(read == -1 ){    //当客户端断开连接时channel.read = -1
                            key.channel();
                        } else {
                            split(buffer);
                            //如果buffer的空间不够，需要扩容(如何判断空间不够，犹豫buffer.compact的方法会将索引放在limit的位置，所以判断索引当前位置和limit值是否相等，就可得知是否空间不足)
                            if(buffer.position() == buffer.limit()){    //相等，则空间不足，需要扩容
                                ByteBuffer newByteBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                //犹豫buffer刚执行完写模式(compact方法)，需要先转化为读模式
                                buffer.flip();
                                newByteBuffer.put(buffer);
                                // 替换掉原有的内存不足的buffer
                                key.attach(newByteBuffer);
                            }
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

    private static void split(ByteBuffer source){
        //开启读模式 --source的索引归0，从头开始读取
        source.flip();
        //计算读取长度
        for (int i = 0; i < source.limit(); i++) {
            if(source.get(i) == '\n'){  //如果获取到\n，向新的buffer里存入
                // =======================第一次循环 ========================
                //存入的长度应该是i+1-索引的当前位置，当前读到第一个\n时，i 的值为2（从0开始读， a、b、\n ），注意 使用get(i)时，source的索引值不会发生变化
                //因为此时没有调用source.get()方法，本身的索引值还是0，所以source.position得到的还是0
                //所以此时的len 的值为 2+1-0=3，可以创建一个长度为3的新buffer缓冲区

                // =======================第二次循环 ========================
                // 存入的长度应该是i+1-索引的当前位置，当前读到第二个\n时，i 的值为5（从3开始读， d、e、\n ），注意 使用get(i)时，source的索引值不会发生变化
                //因为上一次for循环调用source.get()方法，source的初始索引值由0变成了3，所以此时source.position的值为3
                //所以此时的len 的值为 5+1-3=3，还是会创建一个长度为3的新buffer缓冲区
                int len = i + 1 -source.position();
                //创建新的buffer，将读取到的数据存入新的buffer中
                ByteBuffer byteBuffer = ByteBuffer.allocate(len);
                for (int j = 0; j < len; j++) {
                    //当前source是“读”模式，source使用get()方法，索引会发生变化，索引会往后加1
                    byteBuffer.put(source.get());
                    // =======================第一次循环 ========================
                    //此时向新的buffer里存入了三个字符 a、b、\n， 读完\n后，索引往后加1，此时source的索引位置应该是3
                    // =======================第二次循环 ========================
                    //此时向新的buffer里存入了三个字符 d、e、\n， 读完\n后，索引往后加1，此时source的索引位置应该是6

                }
                ByteBufferUtil.debugAll(byteBuffer);
            }
        }
        log.debug("切换读之前的索引：{}",source.position());
        // =======================第一条数据读完(ab\nde\nfg) ========================
        //使用compact方法切换为“写”模式，剩余的不以\n结尾的数据（f、g），压入到source的缓冲区的起始位置中，此时索引位置由6变成了2（从0开始读，读f、g后，索引停留在了g的后面）
        // =======================第二条数据读完(hi\n) ========================
        //使用compact方法切换为“写”模式，已经没有剩余数据，此时索引位置由5变成了0（没有要压入到缓冲区初始位置的数据，因此缓冲区索引位置指向0）
        source.compact();

        log.debug("切换读之后的索引======：{}",source.position());

    }
}
