package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.mixininterface.IThreadExecutor;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.CompletableFuture;

@Mixin(ThreadExecutor.class)
public abstract class MixinThreadExecutor implements IThreadExecutor {


    @Shadow
    protected abstract CompletableFuture<Void> submitAsync(Runnable runnable);

    @Override
    public CompletableFuture<Void> ISubmitAsync(Runnable runnable) {
        return this.submitAsync(runnable);
    }

    @Accessor
    @Override
    public abstract int getExecutionsInProgress();
}
