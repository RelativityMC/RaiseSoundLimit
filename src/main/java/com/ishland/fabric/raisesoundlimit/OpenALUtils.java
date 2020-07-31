package com.ishland.fabric.raisesoundlimit;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;

public class OpenALUtils {

    private static String getErrorMessage(int errorCode) {
        switch(errorCode) {
            case 40961:
                return "Invalid name parameter.";
            case 40962:
                return "Invalid enumerated parameter value.";
            case 40963:
                return "Invalid parameter parameter value.";
            case 40964:
                return "Invalid operation.";
            case 40965:
                return "Unable to allocate memory.";
            default:
                return "An unrecognized error occurred.";
        }
    }

    private static String getAlcErrorMessage(int errorCode) {
        switch(errorCode) {
            case 40961:
                return "Invalid device.";
            case 40962:
                return "Invalid context.";
            case 40963:
                return "Illegal enum.";
            case 40964:
                return "Invalid value.";
            case 40965:
                return "Unable to allocate memory.";
            default:
                return "An unrecognized error occurred.";
        }
    }

    public static boolean checkErrors(String sectionName) {
        int i = AL10.alGetError();
        if (i != 0) {
            FabricLoader.logger.error("AL Error {}: {}", sectionName, getErrorMessage(i));
            return true;
        } else {
            return false;
        }
    }

    static boolean checkAlcErrors(long deviceHandle, String sectionName) {
        int i = ALC10.alcGetError(deviceHandle);
        if (i != 0) {
            FabricLoader.logger.error("ALC Error {}{}: {}", sectionName, deviceHandle, getAlcErrorMessage(i));
            return true;
        } else {
            return false;
        }
    }

}
