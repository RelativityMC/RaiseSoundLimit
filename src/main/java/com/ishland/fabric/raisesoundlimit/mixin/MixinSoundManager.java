package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.MixinUtils;
import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundManager;
import com.ishland.fabric.raisesoundlimit.sound.PooledSoundSystem;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundManager.class)
public abstract class MixinSoundManager implements ISoundManager {

    @Mutable
    @Shadow
    @Final
    private SoundSystem soundSystem;

    @Inject(
            method = "<init>",
            at = @At(
                    value = "RETURN"
            ),
            cancellable = true
    )
    public void onPostInit(ResourceManager resourceManager, GameOptions gameOptions, CallbackInfo ci) throws Exception {
        MixinUtils.logger.info("Initializing PooledSoundSystem");
        soundSystem = new PooledSoundSystem(SoundManager.class.cast(this), gameOptions, resourceManager);
        ci.cancel();
    }

    @Override
    public PooledSoundSystem getSoundSystem() {
        return (PooledSoundSystem) soundSystem;
    }

    ;
}
