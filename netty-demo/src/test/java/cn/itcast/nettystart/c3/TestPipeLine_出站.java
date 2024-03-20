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
 *  ctx.writeAndFlush 方法 寻找出站的handler，是向上查找（按代码编写顺序），而 pipeLine对象的 writeAndFlush方法是向下查找出站 handler
 *
 */

@Slf4j
public class TestPipeLine_出站 {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 1、通过channel 拿到pipeLine
                        ChannelPipeline pipeline = ch.pipeline();
                        // 2、添加处理器 head -> h1 -> h2 -> h3 ->  tail
                        // 添加入站 h1
                        pipeline.addLast("h1",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("1");
                                super.channelRead(ctx, msg);
                            }
                        });
                        // 添加入站 h2
                        pipeline.addLast("h2",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.info("2");
                                super.channelRead(ctx, msg);
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
                        // 添加入站 h3
                        pipeline.addLast("h3",new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                // 接收h2中的数据
                                log.info("3");
                                 super.channelRead(ctx, msg);
                                // hello 是字符串，转换为字节（16进制是 68 65 6c 6c 6f）
                                //ch.writeAndFlush(ctx.alloc().buffer().writeBytes("hello".getBytes()));

                                // ctx执行写入方法时，会向上寻找出站的handler
                                ctx.writeAndFlush(ctx.alloc().buffer().writeBytes("hello".getBytes()));
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
