package cn.itcast.nettystart.c3;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;

/**
 * 讲解 Netty 中的 promise对象，该对象可以填装子线程的返回结果，可以单独创建，而不像之前一样，需要线程的callback回调函数来获取线程结果
 */
@Slf4j
public class TestNettyPromise {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1、准备EventLoop对象
        EventLoop eventLoop = new NioEventLoopGroup().next();
        // 2、主动创建 Promise 结果容器
        DefaultPromise<Object> promise = new DefaultPromise<>(eventLoop);
        // 3、开辟一个新的线程，执行方法
        new Thread(() -> {
            log.info("开始执行任务....");
            try {
                int i = 1/0;
                Thread.sleep(3000);
                // 向promise 填装eventLoop线程的执行结果
                promise.setSuccess(50);
            } catch (InterruptedException e) {
                // 如果抛异常，则promise接收异常数据
                promise.setFailure(e);
                //throw new RuntimeException(e);
            }

        }).start();

        // 4、主线程接收结果
        log.info("主线程等待接收结果....");
        log.info("结果为：{}", promise.get());
    }


}
