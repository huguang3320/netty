package cn.itcast.nettystart.c1;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 了解事件循环组 EventLoopGroup，不光执行IO任务，也可执行普通任务、定时任务
 * 一个channel只绑定一个EventLoop，但是一个EventLoop可以管理多个channel
 */
@Slf4j
public class testEventLoop {
    public static void main(String[] args) {
        //1、创建事件循环组
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup(2);   //开启两个线程，（相当于开了两个EventLoop），如果不填写参数，则默认开启服务器的核心数 * 2个线程（逻辑处理器）
        //2、获取下一个事件循环对象
        System.out.println(eventLoopGroup.next());
        System.out.println(eventLoopGroup.next());
        System.out.println(eventLoopGroup.next());
        System.out.println(eventLoopGroup.next());

        //3、执行普通任务  主线程执行时，该线程也会执行，就是异步执行
//        eventLoopGroup.next().execute(() -> {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//            log.info("ok");
//        });

        //4、执行定时任务
        eventLoopGroup.next().scheduleAtFixedRate(() ->{
            log.info("ok");
        },0,1, TimeUnit.SECONDS);

        log.info("main");
    }
}
