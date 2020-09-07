package com.ishland.fabric.raisesoundlimit.internal;

import com.ishland.fabric.raisesoundlimit.FabricLoader;
import net.minecraft.client.sound.SoundSystem;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public final class SoundSystemPriorityObjectPool extends GenericObjectPool<SoundSystem> {

    private final ReentrantLock sortLock = new ReentrantLock();
    private final AtomicLong lastSorted = new AtomicLong(0L);

    /**
     * Creates a new {@code GenericObjectPool} using defaults from
     * {@link GenericObjectPoolConfig}.
     *
     * @param factory The object factory to be used to create object instances
     *                used by this pool
     */
    public SoundSystemPriorityObjectPool(PooledObjectFactory<SoundSystem> factory) {
        super(factory);
        makePrioritized();
    }

    private void makePrioritized() {
        try {
            final Field idleObjectsField = GenericObjectPool.class.getDeclaredField("idleObjects");
            idleObjectsField.setAccessible(true);
            idleObjectsField.set(this, new PriorityBlockingDeque<>());
        } catch (Throwable t) {
            FabricLoader.logger.warn("Error making queue prioritized", t);
        }
    }
}
