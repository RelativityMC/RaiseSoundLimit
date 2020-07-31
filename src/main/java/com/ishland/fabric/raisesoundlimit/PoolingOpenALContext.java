package com.ishland.fabric.raisesoundlimit;

import com.google.common.base.Preconditions;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class PoolingOpenALContext {

    private static PoolingOpenALContext instance;

    public static PoolingOpenALContext getInstance() {
        if (instance == null) instance = new PoolingOpenALContext();
        return instance;
    }

    private final LinkedBlockingQueue<OpenALContext> pool = new LinkedBlockingQueue<>();
    private final AtomicInteger available = new AtomicInteger(0);

    public PoolingOpenALContext() {
        for (int i = 0; i < FabricLoader.modifier; i++) {
            pool.add(new OpenALContext());
        }
        available.set(FabricLoader.modifier);
    }

    public OpenALContext getContext() throws InterruptedException {
        final OpenALContext poll = pool.poll(500, TimeUnit.MILLISECONDS);
        if (poll != null) available.decrementAndGet();
        return poll;
    }

    public void putContext(OpenALContext context) throws InterruptedException {
        Preconditions.checkNotNull(context);
        Preconditions.checkState(!context.isClosed());
        pool.put(context);
        available.incrementAndGet();
    }

    public int getAvailableCount() {
        return available.get();
    }

}
