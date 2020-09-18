package com.ishland.fabric.raisesoundlimit.mixininterface;

import java.util.concurrent.CompletableFuture;

public interface IThreadExecutor {

    CompletableFuture<Void> ISubmitAsync(Runnable runnable);

    int getExecutionsInProgress();

}
