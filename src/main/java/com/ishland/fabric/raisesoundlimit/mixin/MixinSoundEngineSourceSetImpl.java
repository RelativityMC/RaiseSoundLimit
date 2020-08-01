package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundEngineSourceSetImpl;
import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundManager;
import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundSystem;
import com.ishland.fabric.raisesoundlimit.sound.PooledSoundSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.Source;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(
            method = "createSource",
            at = @At(
                    value = "RETURN"
            )
    )
    public void onPostCreateSource(CallbackInfoReturnable<Source> cir) throws Exception {
        if (cir.getReturnValue() == null) {
            ((ISoundManager)MinecraftClient.getInstance().getSoundManager())
                    .getSoundSystem().tryExtendSize();
        }
    }
}
