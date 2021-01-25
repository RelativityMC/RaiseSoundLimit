package com.ishland.fabric.raisesoundlimit.sound;

import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundSystem;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.sound.SoundInstanceListener;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.resource.ResourceManager;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

class SoundSystemFactory extends BasePooledObjectFactory<SoundSystem> {

    private final SoundManager loader;
    private final GameOptions settings;
    private final ResourceManager resourceManager;
    private final PooledSoundSystem pooledSoundSystem;

    volatile boolean isShuttingDown = false;

    SoundSystemFactory(SoundManager loader,
                       GameOptions settings,
                       ResourceManager resourceManager,
                       PooledSoundSystem pooledSoundSystem) {
        super();
        this.loader = loader;
        this.settings = settings;
        this.resourceManager = resourceManager;
        this.pooledSoundSystem = pooledSoundSystem;
    }

    /**
     * Creates an object instance, to be wrapped in a {@link PooledObject}.
     * <p>This method <strong>must</strong> support concurrent, multi-threaded
     * activation.</p>
     *
     * @return an instance to be served by the pool
     * @throws Exception if there is a problem creating a new instance,
     *                   this will be propagated to the code requesting an object.
     */
    @Override
    public SoundSystem create() throws Exception {
        final SoundSystem soundSystem = new SoundSystem(loader, settings, resourceManager);
        soundSystem.reloadSounds();
        //noinspection ConstantConditions
        if (!((ISoundSystem) soundSystem).isValid())
            throw new IllegalStateException("Creation failed due to verification failure");
        for (SoundInstanceListener listener : pooledSoundSystem.listeners)
            soundSystem.registerListener(listener);
        return soundSystem;
    }

    /**
     * Wrap the provided instance with an implementation of
     * {@link PooledObject}.
     *
     * @param obj the instance to wrap
     * @return The provided instance, wrapped by a {@link PooledObject}
     */
    @Override
    public PooledObject<SoundSystem> wrap(SoundSystem obj) {
        return new DefaultPooledObject<>(obj);
    }

    /**
     * Destroys an instance no longer needed by the pool.
     * <p>
     * It is important for implementations of this method to be aware that there
     * is no guarantee about what state {@code obj} will be in and the
     * implementation should be prepared to handle unexpected errors.
     * </p>
     * <p>
     * Also, an implementation must take in to consideration that instances lost
     * to the garbage collector may never be destroyed.
     * </p>
     *
     * @param p a {@code PooledObject} wrapping the instance to be destroyed
     * @throws Exception should be avoided as it may be swallowed by
     *                   the pool implementation.
     * @see #validateObject
     * @see ObjectPool#invalidateObject
     */
    @Override
    public void destroyObject(PooledObject<SoundSystem> p) throws Exception {
        p.getObject().stopAll();
        p.getObject().stop();
    }

    /**
     * Ensures that the instance is safe to be returned by the pool.
     *
     * @param p a {@code PooledObject} wrapping the instance to be validated
     * @return {@code false} if {@code obj} is not valid and should
     * be dropped from the pool, {@code true} otherwise.
     */
    @Override
    public boolean validateObject(PooledObject<SoundSystem> p) {
        return ((ISoundSystem) p.getObject()).isValid();
    }
}
