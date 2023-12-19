package cn.itcast.netty.c1;

import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

/**
 * 解决黏包问题
 */
@Slf4j
public class TestByteBufferExam {

    /**
     * 此案例需求为 服务器发送三条数据:
       ab\n
       de\n
       fghi\n
      这三条数据中 \n是换行符，结果出现了粘包和半包现象：
       ab\nde\nfg
       hi\n
     */
    public static void main(String[] args) {

        ByteBuffer source = ByteBuffer.allocate(32);
        // a、b、\n 都是字符，使用getBytes转成字节，可以展示成二进制、十进制、十六进制，本案例展示的是十六进制，也就是 a的十六进制是61，b是62，\n是0a
        source.put("ab\nde\nfg".getBytes());
        //使用split方法对这条数据进行拆分
        split(source);
        //模拟发送第二条数据
        source.put("hi\n".getBytes());
         split(source);

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
