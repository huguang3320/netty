package cn.itcast.client;

import cn.itcast.message.*;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 聊天室客户端
 */
@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        // 计数器，它允许一个线程或多个线程等待其他线程完成操作，这个计数器为一个初始化给定的值
        // 每当一个线程调用 countDown 方法时，计数器减一，当计数器达到零时，所有等待在await()方法上的线程会被释放，可以继续运行。

        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);
        AtomicBoolean LOGIN = new AtomicBoolean(false);

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    //ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    // 添加监控空闲读写状态的处理器,用来判断是不是，读 空闲时间太长，或者写 空闲时间太长
                    // 本例子中 如果没有向服务器发送channel的数据，会触发一个IdleState#WRITER_IDLE 事件
                    ch.pipeline().addLast(new IdleStateHandler(0,3,0));
                    // ChannelDuplexHandler 可以同时作为入站和出站处理器
                    ch.pipeline().addLast(new ChannelDuplexHandler(){
                        // 用来触发特殊事件
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent event = (IdleStateEvent) evt;
                            //触发读空闲事件
                            if(event.state() == IdleState.WRITER_IDLE){
                                log.info("已经3s没有写数据了...发送一条数据心跳包");
                                ctx.writeAndFlush(new PingMessage());
                            }
                        }
                    });
                    Scanner sc = new Scanner(System.in);
                    // 添加入站处理器 handler
                    ch.pipeline().addLast("client handler",new ChannelInboundHandlerAdapter(){

                        // 接收响应消息
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.info("---------msg:{}",msg);
                            // 如果消息是登录响应消息
                            if(msg instanceof LoginResponseMessage){
                                LoginResponseMessage response = (LoginResponseMessage) msg;
                                // 如果登录成功，则设置状态为true
                                if(response.isSuccess()){
                                    LOGIN.set(true);
                                }
                                // 计数器减1，则其他阻塞中的线程（使用await方法的线程）可以继续执行
                                WAIT_FOR_LOGIN.countDown();
                            }
                        }

                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // 负责接收用户在控制台的输入，向服务器发送消息
                            // 单独开启线程的原因是 不适用nio线程，避免输入窗口对nio线程进行阻塞
                            new Thread(()-> {
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
                                    // 等待线程释放
                                    WAIT_FOR_LOGIN.await();
                                    // 线程被释放，如果登录失败，则关闭nio线程
                                    if(!LOGIN.get()){
                                        ctx.channel().close();
                                        return;
                                    }
                                    // 线程被释放，如果登录成功，则继续执行后续方法
                                    while (true){
                                        System.out.println("==================================");
                                        System.out.println("send [username] [content]");
                                        System.out.println("gsend [group name] [content]");
                                        System.out.println("gcreate [group name] [m1,m2,m3...]");
                                        System.out.println("gmembers [group name]");
                                        System.out.println("gjoin [group name]");
                                        System.out.println("gquit [group name]");
                                        System.out.println("quit");
                                        System.out.println("==================================");
                                        String command = sc.nextLine();
                                        String[] s = command.split(" ");
                                        switch (s[0]){
                                            case "send":
                                                ctx.writeAndFlush(new ChatRequestMessage(username,s[1],s[2]));
                                                break;
                                            case "gsend":
                                                ctx.writeAndFlush(new GroupChatRequestMessage(username, s[1], s[2]));
                                                break;
                                            case "gcreate":
                                                Set<String> set = new HashSet<>(Arrays.asList(s[2].split(",")));
                                                set.add(username); // 加入自己
                                                ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], set));
                                                break;
                                            case "gmembers":
                                                ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                                break;
                                            case "gjoin":
                                                ctx.writeAndFlush(new GroupJoinRequestMessage(username, s[1]));
                                                break;
                                            case "gquit":
                                                ctx.writeAndFlush(new GroupQuitRequestMessage(username, s[1]));
                                                break;
                                            case "quit":
                                                ctx.channel().close();
                                                return;
                                        }
                                    }


                                } catch (InterruptedException e) {
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
