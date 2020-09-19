package com.ishland.fabric.raisesoundlimit.mixin;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.ishland.fabric.raisesoundlimit.internal.ConcurrentLinkedList;
import com.ishland.fabric.raisesoundlimit.internal.SoundHandleCreationFailedException;
import com.ishland.fabric.raisesoundlimit.mixininterface.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.sound.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(SoundSystem.class)
public abstract class MixinSoundSystem implements ISoundSystem, Comparable<SoundSystem> {

    @Shadow
    private boolean started;

    @Shadow
    @Final
    private SoundExecutor taskQueue;

    @Shadow
    @Final
    private SoundEngine soundEngine;

    @Shadow
    @Final
    private Listener listener;

    @Shadow
    @Final
    private GameOptions settings;

    @Shadow
    @Final
    private SoundLoader soundLoader;

    @Mutable
    @Shadow
    @Final
    private List<Sound> preloadedSounds;

    @Shadow
    @Final
    private static Marker MARKER;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    public abstract void stopAll();

    @Mutable
    @Shadow
    @Final
    private Map<SoundInstance, Channel.SourceManager> sources;
    @Mutable
    @Shadow
    @Final
    private Multimap<SoundCategory, SoundInstance> sounds;
    @Mutable
    @Shadow
    @Final
    private List<TickableSoundInstance> tickingSounds;
    @Mutable
    @Shadow
    @Final
    private Map<SoundInstance, Integer> startTicks;
    @Mutable
    @Shadow
    @Final
    private Map<SoundInstance, Integer> soundEndTicks;
    @Mutable
    @Shadow
    @Final
    private List<SoundInstanceListener> listeners;
    @Mutable
    @Shadow
    @Final
    private List<TickableSoundInstance> soundsToPlayNextTick;
    @Mutable
    @Shadow
    @Final
    private static Set<Identifier> unknownSounds;

    private static boolean isAltered = false;
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final AtomicBoolean isScheduledRemoval = new AtomicBoolean(false);

    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    public void onPostInit(SoundManager loader,
                           GameOptions settings,
                           ResourceManager resourceManager,
                           CallbackInfo ci) {
        if (!isAltered) {
            unknownSounds = Sets.newConcurrentHashSet();
            isAltered = true;
        }
        this.sources = new ConcurrentHashMap<>();
        this.sounds = Multimaps.synchronizedMultimap(this.sounds);
        this.tickingSounds = new ConcurrentLinkedList<>();
        this.startTicks = new ConcurrentHashMap<>();
        this.soundEndTicks = new ConcurrentHashMap<>();
        this.listeners = new ConcurrentLinkedList<>();
        this.soundsToPlayNextTick = new ConcurrentLinkedList<>();
        this.preloadedSounds = new ConcurrentLinkedList<>();
    }

    /**
     * @author ishland
     * @reason start SoundSystem on SoundExecutor
     */
    @Overwrite
    private synchronized void start() throws InterruptedException, ExecutionException, TimeoutException {
        if (!this.started) {
            checkThread();
            ((IThreadExecutor) taskQueue).ISubmitAsync(() -> {
                if (!this.started) {
                    try {
                        this.soundEngine.init();
                        this.listener.init();
                        this.listener.setVolume(this.settings.getSoundVolume(SoundCategory.MASTER));
                        CompletableFuture<?> var10000 = this.soundLoader.loadStatic(this.preloadedSounds);
                        List<Sound> var10001 = this.preloadedSounds;
                        var10000.thenRun(var10001::clear);
                        this.started = true;
                        LOGGER.info(MARKER, "Sound engine started");
                    } catch (RuntimeException var2) {
                        LOGGER.error(MARKER, "Error starting SoundSystem. Turning off sounds & music", var2);
                    }
                }
            }).get(10, TimeUnit.SECONDS); // Run with timeout
        }
    }

    private void checkThread() {
        Preconditions.checkState(
                ((ISoundExecutor) taskQueue).IGetThread().isAlive(),
                "Thread is not alive"
        ); // Check if thread is still there
    }

    /**
     * @author ishland
     * @reason stop SoundSystem on SoundExecutor
     */
    @Overwrite
    public void stop() throws InterruptedException, ExecutionException, TimeoutException {
        if (this.started) {
            checkThread();
            ((IThreadExecutor) taskQueue).ISubmitAsync(() -> {
                this.stopAll();
                this.soundEngine.close();
                this.taskQueue.close();
                this.started = false;
            }).get(10, TimeUnit.SECONDS); // Run with timeout
        }
    }

    @Accessor
    @Override
    public abstract SoundEngine getSoundEngine();

    @Accessor
    @Override
    public abstract SoundExecutor getTaskQueue();

    @Override
    public boolean isValid() {
        return this.started && failedCount.get() <= 8 && !isScheduledRemoval.get();
    }

    @Override
    public int compareTo(SoundSystem soundSystem) {
        return ((ISoundEngine) ((ISoundSystem) soundSystem).getSoundEngine()).getUsages().get(0).getUsed() -
                ((ISoundEngine) soundEngine).getUsages().get(0).getUsed();
    }

    @Redirect(
            method = "play(Lnet/minecraft/client/sound/SoundInstance;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;)V"
            )
    )
    public void onLoggerWarn(Logger logger, String message) {
        if (!message.equals("Failed to create new sound handle")) {
            logger.warn(message);
            return;
        }
        failedCount.incrementAndGet();
        ((ISoundManager) MinecraftClient.getInstance().getSoundManager())
                .getSoundSystem().tryExtendSize();
        throw new SoundHandleCreationFailedException(message);
    }

    @Redirect(
            method = "play(Lnet/minecraft/client/sound/SoundInstance;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;debug(Lorg/apache/logging/log4j/Marker;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"
            )
    )
    public void onPrePlay(Logger logger, Marker marker, String message, Object p0, Object p1) {
        if (!message.equals("Playing sound {} for event {}")) {
            logger.debug(marker, message, p0, p1);
            return;
        }
        failedCount.set(0);
    }

}
