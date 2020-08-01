package com.ishland.fabric.raisesoundlimit.mixininterface;

import com.ishland.fabric.raisesoundlimit.sound.SourceSetUsage;

import java.util.Collection;
import java.util.List;

public interface ISoundEngine {

    List<SourceSetUsage> getUsages();

}
