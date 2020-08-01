package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.MixinUtils;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MixinClientMain {

    @Inject(
            method = "main",
            at = @At(
                    value = "NEW",
                    target = "net/minecraft/client/MinecraftClient",
                    ordinal = 0
            )
    )
    private static void onPreMinecraftClientInit(String[] args, CallbackInfo ci){
        MixinUtils.logger.info("Adding flag to prevent SoundSystem to properly construct");
        MixinUtils.suppressSoundSystemInit = true;
    }

}
