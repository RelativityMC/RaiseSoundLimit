package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.mixininterface.IStaticSound;
import net.minecraft.client.sound.StaticSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

@Mixin(StaticSound.class)
public abstract class MixinStaticSound implements IStaticSound {

    @Override
    @Accessor
    public abstract ByteBuffer getSample();

    @Override
    @Accessor
    public abstract AudioFormat getFormat();

}
