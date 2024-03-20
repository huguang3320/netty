package cn.itcast.nettystart.c3;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 使用 EmbedbedChannel 来模拟客户端发送数据，模拟调用入站、出站handler
 */

@Slf4j
public class TestEmbedbedChannel {
    public static void main(String[] args) {

        // 编写入站 h1
        ChannelInboundHandlerAdapter h1 = new ChannelInboundHandlerAdapter(){
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.info("1");
                super.channelRead(ctx, msg);
            }
        };

        // 编写入站 h2
        ChannelInboundHandlerAdapter h2 = new ChannelInboundHandlerAdapter(){
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                log.info("2");
                super.channelRead(ctx, msg);
            }
        };

        // 编写出站 h3
        ChannelOutboundHandlerAdapter h3 = new ChannelOutboundHandlerAdapter(){
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                log.info("3");
                super.write(ctx, msg, promise);
            }
        };

        // 编写出站 h4
        ChannelOutboundHandlerAdapter h4 = new ChannelOutboundHandlerAdapter(){
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                log.info("4");
                super.write(ctx, msg, promise);
            }
        };

        // 将 h1~h4进行管理
        EmbeddedChannel channel = new EmbeddedChannel(h1,h2,h3,h4);
        // 模拟入站操作 将 消息入站 打印顺序 h1 - h2
        //channel.writeInbound(ByteBufAllocator.DEFAULT.buffer().writeBytes("hello".getBytes()));
        // 模拟出站操作  打印顺序 h4 - h3
        channel.writeOutbound(ByteBufAllocator.DEFAULT.buffer().writeBytes("hello".getBytes()));
    }
}
