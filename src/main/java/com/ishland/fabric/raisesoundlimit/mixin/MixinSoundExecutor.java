package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.MixinUtils;
import net.minecraft.client.sound.SoundExecutor;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(SoundExecutor.class)
public abstract class MixinSoundExecutor extends ThreadExecutor<Runnable> {

    @Shadow
    private volatile boolean stopped;
    @Shadow
    private Thread thread;

    private static final AtomicInteger serial = new AtomicInteger(0);

    protected MixinSoundExecutor(String name) {
        super(name);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void postConstruct(CallbackInfo ci) {
        if (thread != null)
            thread.setPriority(4);
    }

    @Inject(method = "createThread", at = @At("HEAD"), cancellable = true)
    public void onPreCreateThread(CallbackInfoReturnable<Thread> cir) {
        if (MixinUtils.suppressSoundSystemInit) {
            MixinUtils.logger.info("Suppressing SoundSystem SoundExecutor initialization");
            cir.setReturnValue(null);
        }
    }

    @ModifyArg(
            method = "createThread",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/lang/Thread;setName(Ljava/lang/String;)V",
                    ordinal = 0
            )
    )
    public String setThreadName(String threadName){
        return "RSLSound-" + serial.incrementAndGet();
    }

    /**
     * @author ishland
     * @reason dont stop thread
     */
    @Overwrite
    public void restart() {
        this.stopped = true;
        this.cancelTasks();
        this.stopped = false;
    }

    @Override
    public void close() {
        this.stopped = true;
        this.thread.interrupt();
        try {
            this.thread.join();
        } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
        }

        this.cancelTasks();
    }
}
