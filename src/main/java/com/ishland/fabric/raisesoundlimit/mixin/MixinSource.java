package com.ishland.fabric.raisesoundlimit.mixin;

import com.ishland.fabric.raisesoundlimit.MixinLogger;
import com.ishland.fabric.raisesoundlimit.OpenALContext;
import com.ishland.fabric.raisesoundlimit.OpenALUtils;
import com.ishland.fabric.raisesoundlimit.PoolingOpenALContext;
import net.minecraft.client.sound.Source;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Source.class)
public class MixinSource {

    /**
     * @author ishland
     * @reason take over sound system
     */
    @SuppressWarnings("OverwriteModifiers")
    @Overwrite
    public static Source create() throws InterruptedException {
        synchronized (PoolingOpenALContext.class) {
            OpenALContext context = null;
            try {
                context = PoolingOpenALContext.getInstance().getContext();
                if(context == null) return null;
                ALC10.alcMakeContextCurrent(context.getContextPointer());
                int[] is = new int[1];
                AL10.alGenSources(is);
                if (!OpenALUtils.checkErrors("Allocate new source")) {
                    Source.class.getDeclaredConstructor(int.class).newInstance(is[0]);
                } else return create();
            } catch (Throwable e) {
                MixinLogger.logger.error("Unable to create sound source", e);
                return null;
            } finally {
                if (context != null)
                    PoolingOpenALContext.getInstance().putContext(context);
            }
            return null;
        }
    }

}
