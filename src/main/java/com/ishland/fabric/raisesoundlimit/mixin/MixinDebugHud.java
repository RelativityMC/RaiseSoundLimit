package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(DebugHud.class)
public class MixinDebugHud {

    @Inject(
            method = "getRightText",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lcom/google/common/collect/Lists;newArrayList([Ljava/lang/Object;)Ljava/util/ArrayList;",
                    shift = At.Shift.BY,
                    by = 2
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void modifyRightText(CallbackInfoReturnable<List<String>> cir, long l, long m, long n, long o, List<String> list) {
        list.add("");
        list.addAll(((ISoundManager) MinecraftClient.getInstance().getSoundManager())
                .getSoundSystem().getRightDebugString());
    }

}
