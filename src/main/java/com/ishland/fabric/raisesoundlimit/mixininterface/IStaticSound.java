package com.ishland.fabric.raisesoundlimit.mixininterface;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.util.OptionalInt;

public interface IStaticSound {

    int getSampleSize();

    AudioFormat getFormat();

    boolean hasBuffer();

}
