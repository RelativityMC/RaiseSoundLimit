package com.ishland.fabric.raisesoundlimit.mixin;

import com.google.common.util.concurrent.AtomicDouble;
import com.ishland.fabric.raisesoundlimit.internal.SourceNotTimeoutException;
import com.ishland.fabric.raisesoundlimit.internal.SourceTimeoutException;
import com.ishland.fabric.raisesoundlimit.mixininterface.IStaticSound;
import jdk.internal.jline.internal.Nullable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.Source;
import net.minecraft.client.sound.StaticSound;
import net.minecraft.text.LiteralText;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL10;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
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

    @Shadow
    @Nullable
    private AudioStream stream;
    @Shadow
    @Final
    private int pointer;
    @Shadow
    private int bufferSize;
    @Shadow
    @Final
    private static Logger LOGGER;
    private final AtomicInteger lengthMS = new AtomicInteger(0);
    private final AtomicLong startTime = new AtomicLong(-1);
    private final AtomicBoolean timeout = new AtomicBoolean(false);
    private final AtomicDouble pitch = new AtomicDouble(1.0D);

    @Inject(
            method = "setBuffer",
            at = @At("TAIL")
    )
    public void onSetBuffer(StaticSound staticSound, CallbackInfo ci) {
        if (!((IStaticSound) staticSound).hasBuffer()) return;
        lengthMS.set(
                (int) (((IStaticSound) staticSound).getSampleSize() /
                                        (double) getBufferSize(((IStaticSound) staticSound).getFormat(), 1)
                                        * 1000)
        );
    }

    /**
     * @author ishland
     * @reason lengthSec
     */
    @Overwrite
    private void method_19640(int i) {
        if (this.stream != null) {
            try {
                for (int j = 0; j < i; ++j) {
                    ByteBuffer byteBuffer = this.stream.getBuffer(this.bufferSize);
                    if (byteBuffer != null) {
                        (new StaticSound(byteBuffer, this.stream.getFormat())).takeStreamBufferPointer().ifPresent((ix) -> {
                            lengthMS.addAndGet(1000); // Added
                            AL10.alSourceQueueBuffers(this.pointer, new int[]{ix});
                        });
                    }
                }
            } catch (IOException var4) {
                LOGGER.error("Failed to read from audio stream", var4);
            }
        }
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

    @Inject(method = "tick", at = @At("TAIL"))
    public void onPreTick(CallbackInfo ci) {
        if (startTime.get() != -1) {
            if (!isStopped() && startTime.get() != -1 &&
                    (System.currentTimeMillis() - startTime.get()) / pitch.get()
                            > (lengthMS.get() + 3000)) {
                timeout.set(true);
                try {
                    MinecraftClient.getInstance().inGameHud.setOverlayMessage(
                            new LiteralText("Source playing timeout, " + (System.currentTimeMillis() - startTime.get()) / pitch.get() + "/" + (lengthMS.get() + 3000) + "ms"), false);
                } catch (Throwable ignored){
                }
                throw new SourceTimeoutException("Source playing timed out");
            }
            if(isStopped())
                throw new SourceNotTimeoutException();
        }
    }

    @Inject(method = "setPitch", at = @At("HEAD"))
    public void onSetPitch(float f, CallbackInfo ci) {
        this.pitch.set(f);
    }
}
