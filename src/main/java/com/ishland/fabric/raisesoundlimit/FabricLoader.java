package com.ishland.fabric.raisesoundlimit;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FabricLoader implements ModInitializer {

    public static final Logger logger = LogManager.getLogger("RaiseSoundLimit");

    public static final int modifier = 4;

    @Override
    public void onInitialize() {

    }
}
