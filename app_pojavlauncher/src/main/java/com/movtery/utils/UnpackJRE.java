package com.movtery.utils;

import static net.kdt.pojavlaunch.Architecture.archAsString;

import android.content.res.AssetManager;
import android.util.Log;

import net.kdt.pojavlaunch.PojavApplication;
import net.kdt.pojavlaunch.Tools;
import net.kdt.pojavlaunch.multirt.MultiRTUtils;

import java.io.IOException;

public class UnpackJRE {
    public static final String JRE_17_NAME = "Internal-17";
    public static final String JRE_21_NAME = "Internal-21";

    public static void unpackAllJre(AssetManager assetManager) {
        checkInternalJre(assetManager, JRE_17_NAME, 17);
        checkInternalJre(assetManager, JRE_21_NAME, 21);
    }

    public static void checkInternalJre(AssetManager assetManager, String jreName, int jreVersion) {
        String launcherJreVersion;
        String installedJreVersion = MultiRTUtils.readInternalRuntimeVersion(jreName);
        try {
            launcherJreVersion = Tools.read(assetManager.open("components/jre-" + jreVersion + "/version"));
        } catch (IOException exc) {
            return;
        }
        if(!launcherJreVersion.equals(installedJreVersion)) {
            unpackJre(assetManager, jreName, jreVersion, launcherJreVersion);
        }
    }

    private static void unpackJre(AssetManager assetManager, String jreName, int jreVersion, String rtVersion) {
        PojavApplication.sExecutorService.execute(() -> {
            try {
                MultiRTUtils.installRuntimeNamedBinpack(
                        assetManager.open("components/jre-" + jreVersion + "/universal.tar.xz"),
                        assetManager.open("components/jre-" + jreVersion + "/bin-" + archAsString(Tools.DEVICE_ARCHITECTURE) + ".tar.xz"),
                        jreName, rtVersion);
                MultiRTUtils.postPrepare(jreName);
            }catch (IOException e) {
                Log.e("JRE" + jreVersion + "Auto", "Internal JRE unpack failed", e);
            }
        });
    }
}
