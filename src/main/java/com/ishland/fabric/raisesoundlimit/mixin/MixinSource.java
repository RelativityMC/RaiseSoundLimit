package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.*;
import com.ishland.fabric.raisesoundlimit.mixininterface.ISource;
import com.ishland.fabric.raisesoundlimit.mixininterface.IStaticSound;
import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.Source;
import net.minecraft.client.sound.StaticSound;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(Source.class)
public abstract class MixinSource implements ISource {

    @Shadow
    @Final
    private AtomicBoolean playing;
    @Shadow
    @Final
    private int pointer;
    @Shadow
    private AudioStream stream;

    @Shadow private int bufferSize;

    @Shadow
    private static int getBufferSize(AudioFormat format, int time) {
        return 0;
    }

    private OpenALContext context;

    /**
     * @author ishland
     * @reason take over sound system
     */
    @SuppressWarnings("OverwriteModifiers")
    @Overwrite
    public static Source create() throws InterruptedException {
        synchronized (PoolingOpenALContext.getInstance()) {
            OpenALContext context = null;
            try {
                context = PoolingOpenALContext.getInstance().getContext();
                if (context == null) return null;
                ALC10.alcMakeContextCurrent(context.getContextPointer());
                int[] is = new int[1];
                AL10.alGenSources(is);
                if (!OpenALUtils.checkErrors("Allocate new source")) {
                    final Source source = Source.class.getDeclaredConstructor(int.class).newInstance(is[0]);
                    ((ISource) source).setOpenALContext(context);
                    return source;
                } else return create();
            } catch (Throwable e) {
                MixinLogger.logger.error("Unable to create sound source", e);
                return null;
            } finally {
                if (context != null)
                    PoolingOpenALContext.getInstance().putContext(context);
            }
        }
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    public void close() {
        synchronized (PoolingOpenALContext.getInstance()) {
            ALC10.alcMakeContextCurrent(context.getContextPointer());
            if (this.playing.compareAndSet(true, false)) {
                AL10.alSourceStop(this.pointer);
                OpenALUtils.checkErrors("Stop");
                if (this.stream != null) {
                    try {
                        this.stream.close();
                    } catch (IOException var2) {
                        FabricLoader.logger.error("Failed to close audio stream", var2);
                    }

                    this.removeProcessedBuffers();
                    this.stream = null;
                }

                AL10.alDeleteSources(new int[] { this.pointer });
                OpenALUtils.checkErrors("Cleanup");
            }
        }
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    public void play() {
        synchronized (PoolingOpenALContext.getInstance()) {
            ALC10.alcMakeContextCurrent(context.getContextPointer());
            AL10.alSourcePlay(this.pointer);
        }
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    private int getSourceState() {
        if (!this.playing.get()) return 4116;
        synchronized (PoolingOpenALContext.getInstance()) {
            ALC10.alcMakeContextCurrent(context.getContextPointer());
            return AL10.alGetSourcei(this.pointer, 4112);
        }

    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    public void pause() {
        if (this.getSourceState() == 4114) {
            synchronized (PoolingOpenALContext.getInstance()) {
                ALC10.alcMakeContextCurrent(context.getContextPointer());
                AL10.alSourcePause(this.pointer);
            }
        }
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    public void resume() {
        if (this.getSourceState() == 4115) {
            synchronized (PoolingOpenALContext.getInstance()) {
                ALC10.alcMakeContextCurrent(context.getContextPointer());
                AL10.alSourcePlay(this.pointer);
            }
        }
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    public void stop() {
        if (this.playing.get()) {
            synchronized (PoolingOpenALContext.getInstance()) {
                ALC10.alcMakeContextCurrent(context.getContextPointer());
                AL10.alSourceStop(this.pointer);
                OpenALUtils.checkErrors("Stop");
            }
        }
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    public void setPosition(Vec3d vec3d) {
        synchronized (PoolingOpenALContext.getInstance()) {
            ALC10.alcMakeContextCurrent(context.getContextPointer());
            AL10.alSourcefv(this.pointer, 4100,
                    new float[] { (float) vec3d.x, (float) vec3d.y, (float) vec3d.z });
        }
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    public void setPitch(float f) {
        synchronized (PoolingOpenALContext.getInstance()) {
            ALC10.alcMakeContextCurrent(context.getContextPointer());
            AL10.alSourcef(this.pointer, 4099, f);
        }
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    public void setLooping(boolean bl) {
        synchronized (PoolingOpenALContext.getInstance()) {
            ALC10.alcMakeContextCurrent(context.getContextPointer());
            AL10.alSourcei(this.pointer, 4103, bl ? 1 : 0);
        }
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    public void setVolume(float f) {
        synchronized (PoolingOpenALContext.getInstance()) {
            ALC10.alcMakeContextCurrent(context.getContextPointer());
            AL10.alSourcef(this.pointer, 4106, f);
        }
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    public void disableAttenuation() {
        synchronized (PoolingOpenALContext.getInstance()) {
            ALC10.alcMakeContextCurrent(context.getContextPointer());
            AL10.alSourcei(this.pointer, 53248, 0);
        }
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    public void setAttenuation(float f) {
        synchronized (PoolingOpenALContext.getInstance()) {
            ALC10.alcMakeContextCurrent(context.getContextPointer());
            AL10.alSourcei(this.pointer, 53248, 53251);
            AL10.alSourcef(this.pointer, 4131, f);
            AL10.alSourcef(this.pointer, 4129, 1.0F);
            AL10.alSourcef(this.pointer, 4128, 0.0F);
        }
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    public void setRelative(boolean bl) {
        synchronized (PoolingOpenALContext.getInstance()) {
            ALC10.alcMakeContextCurrent(context.getContextPointer());
            AL10.alSourcei(this.pointer, 514, bl ? 1 : 0);
        }
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    public void setBuffer(StaticSound staticSound) {
        ((IStaticSound) staticSound).impl$getStreamBufferPointer().ifPresent((i) -> {
            synchronized (PoolingOpenALContext.getInstance()) {
                ALC10.alcMakeContextCurrent(context.getContextPointer());
                AL10.alSourcei(this.pointer, 4105, i);
            }
        });
    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    private void method_19640(int i) {
        if (this.stream != null) {
            try {
                for(int j = 0; j < i; ++j) {
                    ByteBuffer byteBuffer = this.stream.getBuffer(this.bufferSize);
                    if (byteBuffer != null) {
                        (new StaticSound(byteBuffer, this.stream.getFormat())).takeStreamBufferPointer().ifPresent((ix) -> {
                            synchronized (PoolingOpenALContext.getInstance()){
                                ALC10.alcMakeContextCurrent(context.getContextPointer());
                                AL10.alSourceQueueBuffers(this.pointer, new int[]{ix});
                            }
                        });
                    }
                }
            } catch (IOException var4) {
                FabricLoader.logger.error("Failed to read from audio stream", var4);
            }
        }

    }

    /**
     * @author ishland
     * @reason take over sound system
     */
    @Overwrite
    private int removeProcessedBuffers() {
        synchronized (PoolingOpenALContext.getInstance()){
            ALC10.alcMakeContextCurrent(context.getContextPointer());
            int i = AL10.alGetSourcei(this.pointer, 4118);
            if (i > 0) {
                int[] is = new int[i];
                AL10.alSourceUnqueueBuffers(this.pointer, is);
                OpenALUtils.checkErrors("Unqueue buffers");
                AL10.alDeleteBuffers(is);
                OpenALUtils.checkErrors("Remove processed buffers");
            }

            return i;
        }
    }

    @Override
    public void setOpenALContext(OpenALContext context) {
        this.context = context;
    }
}
