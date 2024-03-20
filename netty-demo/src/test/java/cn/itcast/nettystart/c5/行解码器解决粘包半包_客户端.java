package cn.itcast.nettystart.c5;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * 使用行解码器解决粘包、半包代码
 * 行解码器类似于定义好\n  \r\n这种，遇到这种就认为是一条完整数据
 * 还可以自己定义分隔符，DelimiterBasedFrameDecoder
 */

@Slf4j
public class 行解码器解决粘包半包_客户端 {


    public static StringBuilder makeString(char c, int len){
        StringBuilder sb = new StringBuilder(len+2);
        for (int i = 0; i < len; i++) {
            sb.append(c);
        }
        sb.append("\n");
        return sb;
    }


    private static void send(){
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);  //配置通道
            bootstrap.group(worker);    // 配置组
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                //初始化
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    //添加信息的加工条件
                    ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            ByteBuf buf = ctx.alloc().buffer();
                            char c = '0';
                            Random r = new Random();
                            for (int i = 0; i < 10; i++) {
                                StringBuilder stringBuilder = makeString(c, r.nextInt(256)+1);
                                c++;
                                buf.writeBytes(stringBuilder.toString().getBytes());
                            }
                            ctx.writeAndFlush(buf);
                        }
                    });
                }
            });

            ChannelFuture channelFuture = bootstrap.connect("localhost", 9999).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("client error :{}",e);
        } finally {
            worker.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        send();
    }
}
