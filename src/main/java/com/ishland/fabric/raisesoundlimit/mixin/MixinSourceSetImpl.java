package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.mixininterface.ISourceSetImpl;
import net.minecraft.client.sound.SoundEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.sound.SoundEngine$SourceSetImpl")
public class MixinSourceSetImpl implements ISourceSetImpl {

    @Mutable @Shadow @Final private int maxSourceCount;

    @Override
    public void setMaxSourceCount(int maxSourceCount) {
        this.maxSourceCount = maxSourceCount;
    }
}
