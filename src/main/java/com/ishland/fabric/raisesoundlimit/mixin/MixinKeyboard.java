package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public class MixinKeyboard {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(
            method = "processF3",
            at = @At("RETURN"),
            cancellable = true
    )
    public void onPostProcessF3(int key, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            if (key == 86) {
                ((ISoundManager) MinecraftClient.getInstance().getSoundManager())
                        .getSoundSystem().reloadSounds();
                cir.setReturnValue(true);
                return;
            }
        } else {
            if (key == 81) {
                final ChatHud chatHud = this.client.inGameHud.getChatHud();
                chatHud.addMessage(new LiteralText("F3 + V = Reload SoundSystem"));
                chatHud.addMessage(new LiteralText("F3 + X = Kill stuck SoundSystem executor"));
            }
        }
    }

}
