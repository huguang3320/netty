package cn.itcast.nettystart.c3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 *  pipeLine：相当于流水线， ChannelHandler 相当于流水线的工序，处理流水线上的数据
 *  ChannelHandler分为入站（ChannelInBoundHandlerAdapter 的子类），和出站（ChannelOutBoundHandlerAdapter 的子类）
 *  入站handler主要处理读取客户端数据并写入结果，出站handler主要处理对写入的结果进行架工
 * pipeLine的执行顺序，入站按 h1-h2-h3 的顺序执行，出站按 h6-h5-h4的顺序执行（出站必须有数据写入才会执行）
 *
 *
 * 第二个小案例，在客户端发送消息 “张三"，服务端 h1 对张三这条消息转化成了字符串 ，h2对字符串又转化为Student对象，在h3中将其进行打印（handler之间传递消息是通过msg）
 */

@Slf4j
public class TestPipeLine {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 1、通过channel 拿到pipeLine
                        ChannelPipeline pipeline = ch.pipeline();
                        // 2、添加处理器 head -> h1 -> h2 -> h3 -> tail

                        // 添加入站 h1
                        pipeline.addLast("h1",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("1");
                                // 将 msg 转化为字节
                                ByteBuf buf = (ByteBuf) msg;
                                // 将字节转化为字符  （字节转化字符 可以参考AISCII码表中的编码集对照看，也可以是UTF-8编码集等等，这里采用的默认编码集）
                                String name = buf.toString(Charset.defaultCharset());
                                super.channelRead(ctx, name);
                            }
                        });
                        // 添加入站 h2
                        pipeline.addLast("h2",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("2");
                                // 将h1 传来的 msg 装载到对象中
                                Student s = new Student(msg.toString());
                                super.channelRead(ctx, s);
                            }
                        });
                        // 添加入站 h3
                        pipeline.addLast("h3",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("3，结果:{},class:{}",msg,msg.getClass());
                                // 接收h2中的数据
                                 super.channelRead(ctx, msg);
                                // ctx.alloc().buffer() 创建一个ByteBuf对象，并向对象写入hello的字节数据
                                // hello 是字符串，转换为字节（16进制是 68 65 6c 6c 6f）
                               // ch.writeAndFlush(ctx.alloc().buffer().writeBytes("hello".getBytes()));
                            }
                        });

                        // 添加出站 h4
                        pipeline.addLast("h4",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.info("4");
                                super.write(ctx, msg, promise);
                            }
                        });
                        // 添加出站 h5
                        pipeline.addLast("h5",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.info("5");
                                super.write(ctx, msg, promise);
                            }
                        });
                        // 添加出站 h6
                        pipeline.addLast("h6",new ChannelOutboundHandlerAdapter(){
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.info("6");
                                super.write(ctx, msg, promise);
                            }
                        });

                    }
                }).bind(9999);
    }

    @Data
    @AllArgsConstructor
    static class Student {
        private String name;
    }
}
