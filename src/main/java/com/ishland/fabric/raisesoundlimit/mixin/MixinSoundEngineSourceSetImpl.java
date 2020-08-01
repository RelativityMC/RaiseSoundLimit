package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundEngineSourceSetImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.sound.SoundEngine$SourceSetImpl")
public abstract class MixinSoundEngineSourceSetImpl implements ISoundEngineSourceSetImpl {


    @Shadow public abstract int getMaxSourceCount();

    @Shadow public abstract int getSourceCount();

    @Override
    public int impl$getMaxSourceCount() {
        return getMaxSourceCount();
    }

    @Override
    public int impl$getSourceCount() {
        return getSourceCount();
    }
}
