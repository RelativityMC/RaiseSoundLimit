package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.mixininterface.IStaticSound;
import net.minecraft.client.sound.StaticSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.OptionalInt;

@Mixin(StaticSound.class)
public abstract class MixinStaticSound implements IStaticSound {
    @Shadow
    abstract OptionalInt shadow$getStreamBufferPointer();

    @Override
    public OptionalInt impl$getStreamBufferPointer() {
        return shadow$getStreamBufferPointer();
    }
}
