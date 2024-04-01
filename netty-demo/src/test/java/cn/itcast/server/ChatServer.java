package cn.itcast.server;

import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import cn.itcast.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 聊天业务的服务端
 */


@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        LoginRequestMessageHandler LOGIN_HANDLER = new LoginRequestMessageHandler();
        ChatRequestMessageHandler CHAT_HANDLER = new ChatRequestMessageHandler();
        GroupCreateRequestMessageHandler GROUPCREATE_HANDLER = new GroupCreateRequestMessageHandler();
        GroupChatRequestMessageHandler GROPCHAT_HANDLER = new GroupChatRequestMessageHandler();
        QuitHandler QUIT_HANDLER = new QuitHandler();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                                       // 解决粘包、半包问题的解析器
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    // 日志的解析器
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    // 定义特定的协议 经过编码器 变成byte数组
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    // 添加监控空闲读写状态的处理器,用来判断是不是，读 空闲时间太长，或者写 空闲时间太长
                    // 本例子中 如果没有收到channel的数据，会触发一个IdleState#READER_IDLE 事件
                    ch.pipeline().addLast(new IdleStateHandler(5,0,0));
                    // ChannelDuplexHandler 可以同时作为入站和出站处理器
                    ch.pipeline().addLast(new ChannelDuplexHandler(){
                        // 用来触发特殊事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent event = (IdleStateEvent) evt;
                            //触发读空闲事件
                            if(event.state() == IdleState.READER_IDLE){
                                log.info("已经5s没有读到数据了...");
                            }
                        }
                    });


                    // 登录的处理器
                    ch.pipeline().addLast(LOGIN_HANDLER);
                    // 单聊发送消息的处理器
                    ch.pipeline().addLast(CHAT_HANDLER);
                    // 创建群聊处理器
                    ch.pipeline().addLast(GROUPCREATE_HANDLER);
                    // 发送群聊消息处理器
                    ch.pipeline().addLast(GROPCHAT_HANDLER);
                    // 退出的消息处理器
                    ch.pipeline().addLast(QUIT_HANDLER);
                }
            });
            Channel channel = serverBootstrap.bind(9999).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
