package com.ishland.fabric.raisesoundlimit.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Redirect(
            method = "onPlaySoundId",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"
            )
    )
    public <T extends PacketListener> void onPlaySoundIdForceMainThread(Packet<T> packet,
                                                                        T listener,
                                                                        ThreadExecutor<?> engine) {
    }

    @Redirect(
            method = "onPlaySound",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"
            )
    )
    public <T extends PacketListener> void onPlaySoundForceMainThread(Packet<T> packet,
                                                                      T listener,
                                                                      ThreadExecutor<?> engine) {
    }

}
