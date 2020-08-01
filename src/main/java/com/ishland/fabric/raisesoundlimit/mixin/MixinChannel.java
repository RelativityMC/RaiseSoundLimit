package com.ishland.fabric.raisesoundlimit.mixin;

import net.minecraft.client.sound.Channel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;
import java.util.concurrent.Executor;

@Mixin(Channel.class)
public class MixinChannel {

    @Shadow
    @Final
    private Executor executor;

    @Shadow @Final private Set<Channel.SourceManager> sources;

    /**
     * @author ishland
     * @reason make close execute on thread
     */
    @Overwrite
    public void close() {
        this.executor.execute(() -> {
            this.sources.forEach(Channel.SourceManager::close);
            this.sources.clear();
        });
    }

}
