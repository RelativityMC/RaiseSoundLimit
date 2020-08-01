package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.mixininterface.ISoundManager;
import com.ishland.fabric.raisesoundlimit.sound.PooledSoundSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(DebugHud.class)
public class MixinDebugHud {

    @ModifyVariable(
            method = "getRightText",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lcom/google/common/collect/Lists;newArrayList([Ljava/lang/Object;)Ljava/util/ArrayList;"
            ),
            name = "list"
    )
    public List<String> modifyRightText(List<String> origin) {
        origin.add("");
        origin.addAll(((ISoundManager)MinecraftClient.getInstance().getSoundManager())
                .getSoundSystem().getRightDebugString());
        return origin;
    }

}
