package cn.itcast.netty.c1;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class TestFileChannelTransferTo {

    public static void main(String[] args) {

        try {
            FileChannel from = new FileInputStream("data.txt").getChannel();
            FileChannel to = new FileOutputStream("to.txt").getChannel();

            long size = from.size();
            //定义一个变量left，用于定义剩余字节数，初始值为原始文件的总大小
            for (long left = size;left > 0; ) {
                //from.transferTo(0,from.size(),to); 返回值为已传输的字节数
                //假如文件3个G，超过2G，则会传输两次，第一次传输时，文件总大小和剩余字节总大小是相同的，所以size-left = 0 ，也就是从索引0开始传输，传输left（总大小）个字节到to中
                //当传输超过2个G后，则走第二次循环，第二次循环left = 3G-2.5G ，剩余字节数变成了0.5G，则索引会从2.5G开始传输（也就是size-left），传输0.5个G（也就是left）到to中
                //最后都传输完，left = 0，剩余字节为0 跳出循环
                left = left - from.transferTo((size-left),left,to);
            }




        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
