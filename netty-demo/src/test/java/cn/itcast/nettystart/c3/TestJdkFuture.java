package cn.itcast.nettystart.c3;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 讲解 JDK中的future对象的使用，future相当于一个书包，子线程向futrue中填装数据，主线程等待子线程处理事件，处理结束后主线程通过future.get方法获取子线程返回的数据
 */
@Slf4j
public class TestJdkFuture {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // 1.创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        // 2.提交任务
        Future<Integer> future = executorService.submit(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                log.info("执行计算");
                Thread.sleep(3000);
                return 50;
            }
        });

        //3、主线程通过future来获取结果
        log.info("等待结果....");
        //4、主线程进入等待状态，直到子线程运行完毕，主线程获取结果，future.get是阻塞方法，用于 主线程获取子线程处理的结果
        log.info("获取结果是：{}",future.get());
    }
}
