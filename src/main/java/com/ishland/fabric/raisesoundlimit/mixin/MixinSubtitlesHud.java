package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.internal.ConcurrentLinkedList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.SubtitlesHud;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SubtitlesHud.class)
public class MixinSubtitlesHud {

    @Mutable
    @Shadow
    @Final
    private List<SubtitlesHud.SubtitleEntry> entries;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onPostInit(MinecraftClient client, CallbackInfo ci) {
        entries = new ConcurrentLinkedList<>();
    }

}
