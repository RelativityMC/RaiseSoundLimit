package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.mixininterface.IStaticSound;
import net.minecraft.client.sound.StaticSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.OptionalInt;

@Mixin(StaticSound.class)
public abstract class MixinStaticSound implements IStaticSound {

    @Shadow private boolean hasBuffer;

    private int sampleSize = -1;

    @Override
    @Accessor
    public abstract AudioFormat getFormat();

    @Override
    public boolean hasBuffer() {
        return hasBuffer;
    }

    @Override
    public int getSampleSize() {
        return sampleSize;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(ByteBuffer sample, AudioFormat format, CallbackInfo ci){
        sampleSize = sample.limit();
    }
}
