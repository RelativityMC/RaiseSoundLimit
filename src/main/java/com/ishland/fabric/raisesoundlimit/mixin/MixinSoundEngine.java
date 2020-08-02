package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.MixinUtils;
import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundEngine;
import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundEngineSourceSetImpl;
import com.ishland.fabric.raisesoundlimit.sound.SourceSetUsage;
import net.minecraft.client.sound.SoundEngine;
import org.lwjgl.openal.EXTThreadLocalContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Mixin(SoundEngine.class)
public class MixinSoundEngine implements ISoundEngine {

    @Override
    public List<SourceSetUsage> getUsages() {
        List<SourceSetUsage> list = new LinkedList<>();
        int i = 0;
        for(Field field: getClass().getDeclaredFields()){
            try {
                field.setAccessible(true);
                ISoundEngineSourceSetImpl sourceSet = (ISoundEngineSourceSetImpl) field.get(this);
                list.add(new SourceSetUsage(sourceSet.impl$getSourceCount(), sourceSet.impl$getMaxSourceCount()));
                i ++;
            } catch (ClassCastException | IllegalAccessException ignored){
            }
        }
        return Collections.unmodifiableList(list);
    }

    @Redirect(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/openal/ALC10;alcMakeContextCurrent(J)Z"
            )
    )
    public boolean onAlcMakeContextCurrent(long context){
        MixinUtils.logger.info("Redirecting alcMakeContextCurrent to alcSetThreadContext");
        return EXTThreadLocalContext.alcSetThreadContext(context);
    }

    @Inject(
            method = "close",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/openal/ALC10;alcDestroyContext(J)V"
            )
    )
    public void onBeforeAlcDestroyContext(CallbackInfo ci) {
        EXTThreadLocalContext.alcSetThreadContext(0);
    }

    @Inject(
            method = "close",
            at = @At(
                    value = "TAIL"
            )
    )
    public void onPostClose(CallbackInfo ci) {
        MixinUtils.logger.info("Sound engine closed.");
    }

}
