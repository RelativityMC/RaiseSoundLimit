package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundEngineSourceSetImpl;
import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.Source;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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
            method = "createSource()Lnet/minecraft/client/sound/Source;",
            at = @At(
                    value = "RETURN"
            )
    )
    public void onPostCreateSource(CallbackInfoReturnable<Source> cir) throws Exception {
        if (cir.getReturnValue() == null) {
            ((ISoundManager) MinecraftClient.getInstance().getSoundManager())
                    .getSoundSystem().tryExtendSize();
        }
    }

    @Redirect(
            method = "createSource()Lnet/minecraft/client/sound/Source;",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"
            )
    )
    public void onLoggerWarn(Logger logger, String message, Object p0) {
        if (!message.equals("Maximum sound pool size {} reached")) logger.warn(message, p0);
    }

}
