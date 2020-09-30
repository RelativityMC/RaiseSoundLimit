package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.internal.MixinChannelUtils;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(Channel.class)
public class MixinChannel {

    @Shadow
    @Final
    private Executor executor;

    @Shadow
    @Final
    private Set<Channel.SourceManager> sources;

    private AtomicInteger timeouts = new AtomicInteger(0);

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(SoundEngine soundEngine, Executor executor, CallbackInfo ci) {
        timeouts = new AtomicInteger(0);
    }

    /**
     * @author ishland
     * @reason make close execute on thread
     */
    @Overwrite
    public void close() {
        this.executor.execute(() -> {
            this.sources.forEach(Channel.SourceManager::close);
            this.sources.clear();
        });
    }

    @Inject(method = "createSource", at = @At("HEAD"))
    public void onCreateSource(SoundEngine.RunMode mode, CallbackInfoReturnable<CompletableFuture<Channel.SourceManager>> cir) {
        if (timeouts.get() > 8)
            cir.setReturnValue(CompletableFuture.completedFuture(null));
    }

    /**
     * @author ishland
     * @reason handle tick
     */
    @Overwrite
    public void tick() {
        MixinChannelUtils.tickImpl(this.executor, this.sources, timeouts);

    }

}
