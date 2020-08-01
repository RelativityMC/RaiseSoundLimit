package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundSystem;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(SoundSystem.class)
public abstract class MixinSoundSystem implements ISoundSystem {

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

    /**
     * @author ishland
     * @reason start SoundSystem on SoundExecutor
     */
    @Overwrite
    private synchronized void start() {
        if (!this.started) {
            taskQueue.submitAndJoin(() -> {
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
            });
        }
    }

    /**
     * @author ishland
     * @reason stop SoundSystem on SoundExecutor
     */
    @Overwrite
    public void stop() {
        if (this.started) {
            this.taskQueue.submitAndJoin(() -> {
                this.stopAll();
                this.soundLoader.close();
                this.soundEngine.close();
                this.started = false;
            });
        }
    }

    @Accessor
    @Override
    public abstract SoundEngine getSoundEngine();

    @Accessor
    @Override
    public abstract SoundExecutor getTaskQueue();

}
