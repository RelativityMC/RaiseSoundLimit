package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.FabricLoader;
import com.ishland.fabric.raisesoundlimit.MixinLogger;
import com.ishland.fabric.raisesoundlimit.PoolingOpenALContext;
import com.ishland.fabric.raisesoundlimit.mixininterface.ISourceSetImpl;
import net.minecraft.client.sound.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(SoundEngine.class)
public class MixinSoundEngine {

    @Inject(
            method = "init",
            at = @At("TAIL")
    )
    public void onPostInit(CallbackInfo ci) throws IllegalAccessException {
        for (Field field : getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object object = field.get(this);
            if (object instanceof ISourceSetImpl) {
                final int currentMax = ((ISourceSetImpl) object).getMaxSourceCount();
                MixinLogger.logger.info("Altering " + field.getName() +
                        " from " + currentMax + " to " + currentMax * FabricLoader.modifier);
                ((ISourceSetImpl) object).setMaxSourceCount(currentMax * FabricLoader.modifier);
            }
        }
    }

    @Inject(
            method = "getDebugString",
            at = @At("TAIL")
    )
    public void onPostGetDebugString(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(
                cir.getReturnValue() +
                        String.format(" (%d OpenALContexts available)",
                                PoolingOpenALContext.getInstance().getAvailableCount())
        );
    }

}
