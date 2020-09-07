package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.mixininterface.IStaticSound;
import net.minecraft.client.sound.Source;
import net.minecraft.client.sound.StaticSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Mixin(Source.class)
public abstract class MixinSource {

    @Shadow
    private static int getBufferSize(AudioFormat format, int time) {
        return 0;
    }

    @Shadow
    protected abstract int getSourceState();

    private final AtomicInteger lengthSec = new AtomicInteger(0);
    private final AtomicLong startTime = new AtomicLong(-1);
    private final AtomicBoolean timeout = new AtomicBoolean(false);

    @Inject(
            method = "setBuffer",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/OptionalInt;ifPresent(Ljava/util/function/IntConsumer;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void onSetBuffer(StaticSound staticSound, CallbackInfo ci) {
        final ByteBuffer sample = ((IStaticSound) staticSound).getSample();
        if (sample == null) return;
        lengthSec.set(
                sample.array().length /
                        getBufferSize(((IStaticSound) staticSound).getFormat(), 1)
                        + 2
        );
    }

    @Inject(
            method = "method_19640",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/OptionalInt;ifPresent(Ljava/util/function/IntConsumer;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void onAddBuffer(int i, CallbackInfo ci) {
        lengthSec.incrementAndGet();
    }

    @Inject(method = "play", at = @At("HEAD"))
    public void onPlay(CallbackInfo ci) {
        startTime.set(System.currentTimeMillis());
    }

    /**
     * @author ishland
     * @reason Timeout detection
     */
    @Overwrite
    public boolean isStopped() {
        return timeout.get() || this.getSourceState() == 4116;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void onPreTick(CallbackInfo ci) {
        if (startTime.get() != -1) {
            if (!isStopped() &&
                    System.currentTimeMillis() - startTime.get() > (lengthSec.get() + 1) * 1000)
                timeout.set(true);
        }
    }
}
