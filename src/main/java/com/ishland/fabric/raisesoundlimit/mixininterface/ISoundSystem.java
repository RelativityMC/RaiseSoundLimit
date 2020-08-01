package com.ishland.fabric.raisesoundlimit.mixininterface;

import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.SoundExecutor;

public interface ISoundSystem {

    SoundEngine getSoundEngine();

    SoundExecutor getTaskQueue();

}
