package com.ishland.fabric.raisesoundlimit.internal;

import com.ishland.fabric.raisesoundlimit.mixininterface.ISourceManager;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.Source;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class MixinChannnelUtils {
    public static void tickImpl(Executor executor, Set<Channel.SourceManager> sources, AtomicInteger timeouts) {
        executor.execute(() -> {
            Iterator<Channel.SourceManager> iterator = sources.iterator();

            while (iterator.hasNext()) {
                Channel.SourceManager sourceManager = iterator.next();
                if (sourceManager == null) {
                    iterator.remove();
                    continue;
                }
                final Source source = ((ISourceManager) sourceManager).getSource();
                if (source == null) {
                    iterator.remove();
                    continue;
                }
                try {
                    source.tick();
                } catch (SourceTimeoutException e) {
                    timeouts.incrementAndGet();
                    sourceManager.close();
                    iterator.remove();
                    continue;
                }
                if (source.isStopped()) {
                    sourceManager.close();
                    iterator.remove();
                }
            }

        });

        if (timeouts.get() > 8)
            throw new IllegalStateException("Too many timeouts");
    }
}