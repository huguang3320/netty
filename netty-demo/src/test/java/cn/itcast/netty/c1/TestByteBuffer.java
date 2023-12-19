package cn.itcast.netty.c1;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class TestByteBuffer {


    public static void main(String[] args) {


        try {
            //输入输出流
            FileChannel channel = new FileInputStream("data.txt").getChannel();
            //准备缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (true){
                //从channel中读取数据，然后写入到byteBuffer中
                int len = channel.read(buffer);
                log.debug("读到的字节数：{}",len);
                if(len == -1){
                    break;
                }
                //切换到读模式
                buffer.flip();
                while (buffer.hasRemaining()){  //是否还有剩余字节未读
                    byte b = buffer.get();
                    log.debug("实际字节：{}",(char) b); //将字节强转成字符 打印
                }
                //切换为写模式
                buffer.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
