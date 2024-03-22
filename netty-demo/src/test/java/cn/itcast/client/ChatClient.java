package cn.itcast.client;

import cn.itcast.message.LoginRequestMessage;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Scanner;

/**
 * 聊天室客户端
 */
@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    // 添加入站处理器 handler
                    ch.pipeline().addLast("client handler",new ChannelInboundHandlerAdapter(){

                        // 接收响应消息
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.info("---------msg:{}",msg);
                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // 负责接收用户在控制台的输入，向服务器发送消息
                            // 单独开启线程的原因是 不适用nio线程，避免输入窗口对nio线程进行阻塞
                            new Thread(()-> {
                                Scanner sc = new Scanner(System.in);
                                System.out.println("请输入用户名：");
                                String username = sc.nextLine();
                                System.out.println("请输入密码：");
                                String password = sc.nextLine();
                                // 构造消息对象
                                LoginRequestMessage loginRequestMessage = new LoginRequestMessage(username, password);
                                // 写入内容，触发出站操作 执行 MESSAGE_CODEC、LOGGING_HANDLER、ProcotolFrameDecoder
                                ctx.writeAndFlush(loginRequestMessage);

                                System.out.println("等待后续操作.....");
                                try {
                                    System.in.read();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            },"system in").start();


                        }
                    });
                }
            });
            Channel channel = bootstrap.connect("localhost", 9999).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
