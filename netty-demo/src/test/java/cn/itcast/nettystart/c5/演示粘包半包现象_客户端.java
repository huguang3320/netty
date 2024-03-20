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
/**
 * 演示粘包、半包代码
 */

@Slf4j
public class 演示粘包半包现象_客户端 {


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
                            for (int i = 0; i < 10; i++) {

                                ByteBuf buffer = ctx.alloc().buffer(16);
                                buffer.writeBytes(new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});
                                ctx.writeAndFlush(buffer);
                            }
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
