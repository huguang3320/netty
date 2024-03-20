package cn.itcast.nettystart.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 讲解 Netty中的future对象的使用，future相当于一个书包，子线程向futrue中填装数据，主线程等待子线程处理事件，处理结束后主线程通过future.get方法获取子线程返回的数据
 * 与JDK 中的future对象不同的是，netty可以使用eventLoop（子线程）异步获取结果对象（本段代码的第二种实现方式）
 */
@Slf4j
public class TestNettyFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 创建eventLoop组，管理多个eventLoop，每个eventLoop有一个线程
        NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
        // 获取eventLoop对象
        EventLoop eventLoop = nioEventLoopGroup.next();
        // 使用eventLoop线程执行代码，并获取返回结果，类型是Integer
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.info("eventLoop线程正在执行任务....");
                Thread.sleep(3000);
                return 50;
            }
        });

        // 获取结果数据。第一种方法，主线程同步获取结果数据, 这种方式与JDKfuture是相同的
        /**
         *         log.info("正在等待结果....");
         *         log.info("结果为：{}",future.get());
         */


        // 获取结果数据。第二种方法，使用eventLoop线程异步等待获取数据结果
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                //getNow() 和 get() 相同，getNow是立即获取结果
                log.info("接收结果:{}",future.getNow());
            }
        });


    }
}
