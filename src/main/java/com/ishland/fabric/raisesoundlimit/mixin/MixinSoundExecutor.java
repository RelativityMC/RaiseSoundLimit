package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.MixinUtils;
import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundExecutor;
import com.ishland.fabric.raisesoundlimit.mixininterface.IThreadExecutor;
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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(SoundExecutor.class)
public abstract class MixinSoundExecutor extends ThreadExecutor<Runnable> implements ISoundExecutor {

    @Shadow
    private volatile boolean stopped;
    @Shadow
    private Thread thread;

    private static final AtomicInteger serial = new AtomicInteger(0);

    private LinkedBlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

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

    @Override
    public Thread IGetThread() {
        return this.getThread();
    }

    @Override
    public int getTaskCount() {
        return tasks.size();
    }

    @Override
    public void send(Runnable runnable) {
        tasks.add(runnable);
    }

    @Override
    protected void cancelTasks() {
        tasks.clear();
    }

    @Override
    protected boolean runTask() {
        Runnable runnable;
        try {
            runnable = tasks.poll(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            return true;
        }
        if (runnable == null) return true;
        if (((IThreadExecutor) this).getExecutionsInProgress() == 0 && !this.canExecute(runnable))
            return true;
        this.executeTask(runnable);
        return true;
    }

    /**
     * @author ishland
     * @reason no waiting
     */
    @Overwrite
    protected void waitForTasks() {
    }
}
