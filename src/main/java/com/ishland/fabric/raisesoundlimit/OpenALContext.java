package com.ishland.fabric.raisesoundlimit;

import com.google.common.base.Preconditions;
import org.lwjgl.openal.*;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class OpenALContext implements Closeable {

    private long devicePointer = -1L;
    private long contextPointer = -1L;
    private boolean isClosed = false;

    public OpenALContext() {
        for (int i = 0; i < 3; ++i) {
            long l = ALC10.alcOpenDevice((ByteBuffer) null);
            if (l != 0L && !OpenALUtils.checkAlcErrors(l, "Open device")) {
                devicePointer = l;
                break;
            }
        }
        if (devicePointer == -1L)
            throw new IllegalStateException("Failed to open device");
        ALCCapabilities aLCCapabilities = ALC.createCapabilities(devicePointer);
        if (OpenALUtils.checkAlcErrors(this.devicePointer, "Get capabilities")) {
            throw new IllegalStateException("Failed to get OpenAL capabilities");
        } else if (!aLCCapabilities.OpenALC11) {
            throw new IllegalStateException("OpenAL 1.1 not detected");
        } else {
            this.contextPointer = ALC10.alcCreateContext(this.devicePointer, (IntBuffer) null);
        }

        ALCapabilities aLCapabilities = AL.createCapabilities(aLCCapabilities);
        OpenALUtils.checkErrors("Initialization");

        if (!aLCapabilities.AL_EXT_source_distance_model) {
            throw new IllegalStateException("AL_EXT_source_distance_model is not supported");
        } else {
            AL10.alEnable(512);
            if (!aLCapabilities.AL_EXT_LINEAR_DISTANCE) {
                throw new IllegalStateException("AL_EXT_LINEAR_DISTANCE is not supported");
            } else {
                OpenALUtils.checkErrors("Enable per-source distance models");
            }
        }

        FabricLoader.logger.info("Created OpenALContext with device " +
                devicePointer + " context " + contextPointer);

    }

    public long getDevicePointer() {
        Preconditions.checkState(!isClosed);
        return devicePointer;
    }

    public long getContextPointer() {
        Preconditions.checkState(!isClosed);
        return contextPointer;
    }

    public boolean isClosed() {
        return isClosed;
    }

    /**
     * @deprecated
     */
    @Override
    @SuppressWarnings("deprecation")
    protected void finalize() throws Throwable {
        super.finalize();
        this.close();
    }

    @Override
    public void close() {
        Preconditions.checkState(!isClosed);
        if (ALC10.alcGetCurrentContext() == this.contextPointer)
            ALC10.alcMakeContextCurrent(-1);
        ALC10.alcDestroyContext(this.contextPointer);
        if (this.devicePointer != 0L) {
            ALC10.alcCloseDevice(this.devicePointer);
        }
        isClosed = true;
        FabricLoader.logger.info("Closed OpenALContext with device " +
                devicePointer + " context " + contextPointer);
    }
}
