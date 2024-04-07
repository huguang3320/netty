package cn.itcast.client;

import cn.itcast.client.handler.RpcResponseMessageHandler;
import cn.itcast.message.RpcRequestMessage;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import cn.itcast.protocol.SequenceIdGenerator;
import cn.itcast.server.service.HelloService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * 创建 channel 对象,只在初始化的时候执行一次
 * 整个代码过程分为二个线程：
 *      主线程：执行整个代码段中的各个部分
 *      EventLoop线程：启用一个新的线程，负责与服务端连接，sync会阻塞主线程，直到连接操作完成
 */
@Slf4j
public class RpcClientManager {

    private static Channel channel = null;
    private static final Object LOCK = new Object();

    public static void main(String[] args) throws InterruptedException {

        HelloService service = getProxyService(HelloService.class);
        System.out.println(service.sayHello("张三"));

    }
    // 创建代理类 使用代理类来将消息编辑后 并发送给服务端
    // 这段代码的主要目的是通过动态代理来拦截对某个接口方法的调用，并将方法调用转换为消息对象发送出去
    public static <T> T getProxyService(Class<T> serviceClass) throws InterruptedException {
        // 类的加载器 用于加载动态代理类
        ClassLoader loader = serviceClass.getClassLoader();
        // 代理类需要实现的接口列表  这段代码含义是创建一个新的Class类型的数组，并且将serviceClass作为数组的唯一元素
        Class<?>[] interfaces = new Class[]{serviceClass};
        int sequenceId = SequenceIdGenerator.nextId();
        Object o = Proxy.newProxyInstance(loader ,interfaces, (proxy, method ,args) ->{
            // 1 将方法调用转换为消息对象
             RpcRequestMessage msg =  new RpcRequestMessage(
                     sequenceId,   // SequenceId
                    serviceClass.getName(),         // 类名
                    method.getName(),               // 方法名
                    method.getReturnType(),         // 返回值类型
                    method.getParameterTypes(),     // 参数类型
                    args                // 实际的参数

            );

        // 2.  将消息对象发送出去
        getChannel().writeAndFlush(msg);
        // 3.  获取响应信息（相当于发送一个空的书包） , 定义用哪个线程来填装数据到promise中，使用 getChannel().eventLoop() 方法可以将线程传递过去
            DefaultPromise<Object> promise = new DefaultPromise<>(getChannel().eventLoop());
            // 将  sequenceId 和背包 存放到handler的map中
            RpcResponseMessageHandler.PROMISES.put(sequenceId, promise);

            // 4. 等待背包被装填数据 类似于阻塞，只有promise中有数据，才会释放
            promise.await();
            if(promise.isSuccess()){
                return promise.getNow();
            } else {
                return new RuntimeException(promise.cause());
            }
      });
        return (T) o;
    }
    public static Channel getChannel() throws InterruptedException {
        // 如果线程拿到channel不为空，直接返回channel
        if(channel != null){
            return channel;
        }
            //使用锁，防止多个线程进入后 同时创建channel
        synchronized (LOCK){
             // 如果 t1 线程进入后 创建了channel，这时 t2 线程进入后 直接返回channel，无需再创建
            if(channel != null){
                return channel;
            }
            initChannel();
            return channel;
        }
    }

    // 初始化channel
    private static void initChannel() throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        RpcResponseMessageHandler RPC_HANDLER = new RpcResponseMessageHandler();

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ProcotolFrameDecoder()); // 入站处理器
                    ch.pipeline().addLast(LOGGING_HANDLER); // 出、入站处理器
                    ch.pipeline().addLast(MESSAGE_CODEC);   // 出、入站处理器
                    ch.pipeline().addLast(RPC_HANDLER);     // 入站处理器    --等待消息接收
                }
            });
            channel = bootstrap.connect("localhost", 9999).sync().channel();
        try {
            // 增加一个监听，当channel关闭的时候触发
            channel.closeFuture().addListener(future -> {
                group.shutdownGracefully();
            });
        } catch (Exception e) {
            log.error("client error", e);
        }
    }
}
