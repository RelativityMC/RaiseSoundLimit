package com.ishland.fabric.raisesoundlimit.mixin;

import net.minecraft.client.sound.Source;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Source.class)
public class MixinSource {

    @Inject(method = "tick", at = @At("HEAD"))
    public void onPreTick(CallbackInfo ci) {

    }

}
