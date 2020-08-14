package com.ishland.fabric.raisesoundlimit.internal;

import net.minecraft.client.sound.SoundSystem;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

public final class SoundSystemPriorityObjectPool extends GenericObjectPool<SoundSystem> {
    /**
     * Creates a new {@code GenericObjectPool} using defaults from
     * {@link GenericObjectPoolConfig}.
     *
     * @param factory The object factory to be used to create object instances
     *                used by this pool
     */
    public SoundSystemPriorityObjectPool(PooledObjectFactory<SoundSystem> factory) {
        super(factory);
    }

    /**
     * Borrows an object from the pool using the specific waiting time which only
     * applies if {@link #getBlockWhenExhausted()} is true.
     * <p>
     * This implementation additionally sort
     * </p>
     * <p>
     * If there is one or more idle instance available in the pool, then an
     * idle instance will be selected based on the value of {@link #getLifo()},
     * activated and returned. If activation fails, or {@link #getTestOnBorrow()
     * testOnBorrow} is set to {@code true} and validation fails, the
     * instance is destroyed and the next available instance is examined. This
     * continues until either a valid instance is returned or there are no more
     * idle instances available.
     * </p>
     * <p>
     * If there are no idle instances available in the pool, behavior depends on
     * the {@link #getMaxTotal() maxTotal}, (if applicable)
     * {@link #getBlockWhenExhausted()} and the value passed in to the
     * {@code borrowMaxWaitMillis} parameter. If the number of instances
     * checked out from the pool is less than {@code maxTotal,} a new
     * instance is created, activated and (if applicable) validated and returned
     * to the caller. If validation fails, a {@code NoSuchElementException}
     * is thrown.
     * </p>
     * <p>
     * If the pool is exhausted (no available idle instances and no capacity to
     * create new ones), this method will either block (if
     * {@link #getBlockWhenExhausted()} is true) or throw a
     * {@code NoSuchElementException} (if
     * {@link #getBlockWhenExhausted()} is false). The length of time that this
     * method will block when {@link #getBlockWhenExhausted()} is true is
     * determined by the value passed in to the {@code borrowMaxWaitMillis}
     * parameter.
     * </p>
     * <p>
     * When the pool is exhausted, multiple calling threads may be
     * simultaneously blocked waiting for instances to become available. A
     * "fairness" algorithm has been implemented to ensure that threads receive
     * available instances in request arrival order.
     * </p>
     *
     * @param borrowMaxWaitMillis The time to wait in milliseconds for an object
     *                            to become available
     * @return object instance from the pool
     * @throws NoSuchElementException if an instance cannot be returned
     * @throws Exception              if an object instance cannot be returned due to an
     *                                error
     */
    @Override
    public SoundSystem borrowObject(long borrowMaxWaitMillis) throws Exception {

        return super.borrowObject(borrowMaxWaitMillis);
    }

    @SuppressWarnings("unchecked")
    private LinkedBlockingQueue<PooledObject<SoundSystem>> getQueue() {
        try {
            final Field idleObjectsField = GenericObjectPool.class.getDeclaredField("idleObjects");
            idleObjectsField.setAccessible(true);
            return (LinkedBlockingQueue<PooledObject<SoundSystem>>) idleObjectsField.get(this);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
