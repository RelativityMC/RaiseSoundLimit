package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.MixinLogger;
import com.ishland.fabric.raisesoundlimit.mixininterface.ISourceSetImpl;
import net.minecraft.client.sound.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;

@Mixin(SoundEngine.class)
public class MixinSoundEngine {

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    public void onPostInit(CallbackInfo ci) throws IllegalAccessException {
        MixinLogger.logger.info("Altering sound sources to 512");
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object object = field.get(this);
            if (object instanceof ISourceSetImpl) {
                MixinLogger.logger.info("Altering " + field.getName() + " to 512");
                ((ISourceSetImpl) object).setMaxSourceCount(512);
            }
        }
    }

}
