package cn.itcast.server.handler;

import cn.itcast.message.ChatRequestMessage;
import cn.itcast.message.ChatResponseMessage;
import cn.itcast.message.LoginRequestMessage;
import cn.itcast.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 聊天的handler
 */
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        //获取目的地的用户名
        String msgTo = msg.getTo();
        //判断目的地是否在线
        Channel channel = SessionFactory.getSession().getChannel(msgTo);
        if(channel != null){    //在线
            //讲消息发送给目的地
            channel.writeAndFlush(new ChatResponseMessage(msg.getFrom(),msg.getContent()));
        } else {
            ctx.writeAndFlush(new ChatResponseMessage(false,"对方不在线"));
        }

    }
}
