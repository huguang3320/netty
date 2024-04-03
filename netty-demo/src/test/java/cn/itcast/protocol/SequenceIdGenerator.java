package cn.itcast.protocol;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *  自动生成id 自增
 */
public abstract class SequenceIdGenerator {
    private static final AtomicInteger id = new AtomicInteger();

    public static int nextId() {
        return id.incrementAndGet();
    }
}
