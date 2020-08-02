package com.ishland.fabric.raisesoundlimit.sound;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.ishland.fabric.raisesoundlimit.FabricLoader;
import com.ishland.fabric.raisesoundlimit.MixinUtils;
import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundEngine;
import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.Camera;
import net.minecraft.client.sound.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class PooledSoundSystem extends SoundSystem {

    private static final Supplier<Field> allSoundSystemField = Suppliers.memoize(() -> {
        Field field;
        try {
            field = GenericObjectPool.class.getDeclaredField("allObjects");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        field.setAccessible(true);
        return field;
    });
    private static final String calculatingDebugString = "Calculating...";

    // Executors and pools
    private final GenericObjectPool<SoundSystem> pool;
    private final ThreadPoolExecutor internalExecutor =
            new ThreadPoolExecutor(1, Math.max(Runtime.getRuntime().availableProcessors() - 1, 1),
                    60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                    new ThreadFactory() {
                        private final AtomicLong serial = new AtomicLong(0);

                        @Override
                        public Thread newThread(Runnable runnable) {
                            final Thread thread = new Thread(runnable);
                            thread.setName("PooledSoundSystem Executor - " + serial.incrementAndGet());
                            return thread;
                        }
                    });

    private long lastFetchTime = 0L;
    private String debugString = calculatingDebugString;
    private final AtomicBoolean isResourceLoaded = new AtomicBoolean(false);

    public PooledSoundSystem(SoundManager loader, GameOptions settings, ResourceManager resourceManager) throws Exception {
        super(loader, settings, resourceManager);
        MixinUtils.logger.info("Removing flags for SoundSystem initialization prevention");
        MixinUtils.suppressSoundSystemInit = false;
        // Constructor arguments for SoundSystem
        this.pool = new GenericObjectPool<>(new SoundSystemFactory(loader, settings, resourceManager));
        pool.setMinIdle(Runtime.getRuntime().availableProcessors());
        pool.setMaxIdle(Runtime.getRuntime().availableProcessors() * 8);
        pool.setMaxTotal(Runtime.getRuntime().availableProcessors() * 8);
        pool.setLifo(false);
    }

    public void tryExtendSize() throws Exception {
        if (pool.getNumActive() + pool.getNumIdle() < pool.getMaxTotal()) {
            FabricLoader.logger.info("Stopping sounds and extending size of sound system");
            MinecraftClient.getInstance().execute(() ->
                    MinecraftClient.getInstance().inGameHud.setOverlayMessage(
                            new LiteralText("Stopping sounds and extending size of sound system"),
                            false
                    ));
            stopAll();
            pool.addObject();
        }
    }

    @Override
    public void reloadSounds() {
        pool.clear();
        try {
            pool.preparePool();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        isResourceLoaded.set(true);
    }

    @Override
    public void updateSoundVolume(SoundCategory soundCategory, float volume) {
        soundSystemForEach(soundSystem -> soundSystem.updateSoundVolume(soundCategory, volume), true);
    }

    @Override
    public void stop() {
        pool.close();
        internalExecutor.shutdown();
    }

    @Override
    public void stop(SoundInstance soundInstance) {
        soundSystemForEach(soundSystem -> soundSystem.stop(soundInstance), false);
    }

    @Override
    public void stopAll() {
        soundSystemForEach(SoundSystem::stopAll, false);
    }

    @Override
    public void registerListener(SoundInstanceListener soundInstanceListener) {
        soundSystemForEach(soundSystem -> soundSystem.registerListener(soundInstanceListener), true);
    }

    @Override
    public void unregisterListener(SoundInstanceListener soundInstanceListener) {
        soundSystemForEach(soundSystem -> soundSystem.unregisterListener(soundInstanceListener), true);
    }

    @Override
    public void tick(boolean bl) {
        if (!isResourceLoaded.get()) return;
        internalExecutor.execute(() -> {
            try {
                pool.preparePool();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        soundSystemForEach(soundSystem -> soundSystem.tick(bl), true);
    }

    @Override
    public boolean isPlaying(SoundInstance soundInstance) {
        AtomicBoolean isPlaying = new AtomicBoolean(false);
        soundSystemForEach(soundSystem -> {
            if (soundSystem.isPlaying(soundInstance)) {
                isPlaying.set(true);
                throw new BreakException();
            }
        }, false);
        return isPlaying.get();
    }

    @Override
    public void play(SoundInstance soundInstance) {
        internalExecutor.execute(() -> {
            final SoundSystem soundSystem;
            try {
                soundSystem = pool.borrowObject();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                soundSystem.play(soundInstance);
            } catch (Throwable t) {
                try {
                    pool.invalidateObject(soundSystem);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            pool.returnObject(soundSystem);
        });
    }

    @Override
    public void playNextTick(TickableSoundInstance sound) {
        internalExecutor.execute(() -> {
            final SoundSystem soundSystem;
            try {
                soundSystem = pool.borrowObject();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                soundSystem.playNextTick(sound);
            } catch (Throwable t) {
                try {
                    pool.invalidateObject(soundSystem);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            pool.returnObject(soundSystem);
        });
    }

    @Override
    public void addPreloadedSound(Sound sound) {
        soundSystemForEach(soundSystem -> soundSystem.addPreloadedSound(sound), true);
    }

    @Override
    public void pauseAll() {
        soundSystemForEach(SoundSystem::pauseAll, false);
    }

    @Override
    public void resumeAll() {
        soundSystemForEach(SoundSystem::resumeAll, false);
    }

    @Override
    public void play(SoundInstance sound, int delay) {
        internalExecutor.execute(() -> {
            final SoundSystem soundSystem;
            try {
                soundSystem = pool.borrowObject();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            try {
                soundSystem.play(sound, delay);
            } catch (Throwable t) {
                try {
                    pool.invalidateObject(soundSystem);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            pool.returnObject(soundSystem);
        });
    }

    @Override
    public void updateListenerPosition(Camera camera) {
        soundSystemForEach(soundSystem -> soundSystem.updateListenerPosition(camera), true);
    }

    @Override
    public void stopSounds(Identifier identifier, SoundCategory soundCategory) {
        soundSystemForEach(soundSystem -> soundSystem.stopSounds(identifier, soundCategory), true);
    }

    @Override
    public String getDebugString() {
        if (Math.abs(lastFetchTime - System.currentTimeMillis()) > 10 * 1000)
            debugString = calculatingDebugString;
        internalExecutor.execute(() -> {
            List<List<SourceSetUsage>> list = new LinkedList<>();
            soundSystemForEach(soundSystem ->
                    list.add(((ISoundEngine) ((ISoundSystem) soundSystem).getSoundEngine()).getUsages()), false);
            int[] used = new int[list.get(0).size()];
            int[] max = new int[list.get(0).size()];
            list.forEach(sourceSetUsages -> {
                for (int i = 0, sourceSetUsagesLength = sourceSetUsages.size(); i < sourceSetUsagesLength; i++) {
                    SourceSetUsage usage = sourceSetUsages.get(i);
                    used[i] += usage.getUsed();
                    max[i] += usage.getMax();
                }
            });
            StringBuilder builder = new StringBuilder();
            for (int i = 0, length = used.length; i < length; i++) {
                builder.append(used[i]).append("/").append(max[i]);
                if (i + 1 < length)
                    builder.append(" + ");
            }
            debugString = builder.toString();
        });
        lastFetchTime = System.currentTimeMillis();
        return "Sound: " + debugString; // For performance, reflection is expensive
    }

    public List<String> getRightDebugString() {
        List<String> list = new LinkedList<>();
        list.add(String.format("SoundSystem pool size: %d/%d/%d",
                pool.getNumActive(), pool.getNumActive() + pool.getNumIdle(), pool.getMaxTotal()));
        list.add(String.format("SoundSystem executor size: %d/%d/%d",
                internalExecutor.getActiveCount(), internalExecutor.getPoolSize(), internalExecutor.getMaximumPoolSize()));
        return list;
    }

    private void soundSystemForEach(Consumer<SoundSystem> consumer, boolean useExecutor) {
        final Collection<PooledObject<SoundSystem>> systems;
        try {
            //noinspection unchecked
            systems = ((Map<?, PooledObject<SoundSystem>>) allSoundSystemField.get().get(pool)).values();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            for (PooledObject<SoundSystem> pooledSystem : systems) {
                if (useExecutor)
                    internalExecutor.execute(() -> consumer.accept(pooledSystem.getObject()));
                else
                    consumer.accept(pooledSystem.getObject());
            }
        } catch (BreakException ignored) {
        }
    }

    private static final class BreakException extends RuntimeException {

    }

}
