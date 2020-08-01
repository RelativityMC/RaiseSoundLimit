package com.ishland.fabric.raisesoundlimit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MixinUtils {

    public static final Logger logger = LogManager.getLogger("RaiseSoundLimit|Mixins");
    public static boolean suppressSoundSystemInit = false;

}
