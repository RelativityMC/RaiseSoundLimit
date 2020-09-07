package com.ishland.fabric.raisesoundlimit.mixininterface;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

public interface IStaticSound {

    ByteBuffer getSample();

    AudioFormat getFormat();

}
