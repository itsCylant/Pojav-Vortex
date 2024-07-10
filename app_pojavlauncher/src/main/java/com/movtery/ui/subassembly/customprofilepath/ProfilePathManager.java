package com.movtery.ui.subassembly.customprofilepath;

import static net.kdt.pojavlaunch.prefs.LauncherPreferences.DEFAULT_PREF;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.movtery.feature.ResourceManager;

import net.kdt.pojavlaunch.Tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ProfilePathManager {
    private static final String defaultPath = Tools.DIR_GAME_HOME;

    public static void setCurrentPathId(String id) {
        DEFAULT_PREF.edit().putString("launcherProfile", id).apply();
    }

    public static String getCurrentPath() {
        //Use the selected ID to get the current path
        String id = DEFAULT_PREF.getString("launcherProfile", "default");
        if (id.equals("default")) {
            return defaultPath;
        }

        if (Tools.FILE_PROFILE_PATH.exists()) {
            try {
                String read = Tools.read(Tools.FILE_PROFILE_PATH);
                JsonObject jsonObject = JsonParser.parseString(read).getAsJsonObject();
                if (jsonObject.has(id)) {
                    ProfilePathJsonObject profilePathJsonObject = new Gson().fromJson(jsonObject.get(id), ProfilePathJsonObject.class);
                    return profilePathJsonObject.path;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return defaultPath;
    }

    public static File getCurrentProfile() {
        File file = new File(ProfilePathHome.getGameHome(), "launcher_profiles.json");
        if (!file.exists()) {
            try {
                Tools.copyAssetFile(ResourceManager.getContext(), "launcher_profiles.json", ProfilePathHome.getGameHome(), false);
            } catch (IOException e) {
                return new File(defaultPath, "launcher_profiles.json");
            }
        }
        return file;
    }

    public static void save(List<ProfileItem> items) {
        JsonObject jsonObject = new JsonObject();

        for (ProfileItem item : items) {
            if (item.id.equals("default")) continue;

            ProfilePathJsonObject profilePathJsonObject = new ProfilePathJsonObject();
            profilePathJsonObject.title = item.title;
            profilePathJsonObject.path = item.path;
            jsonObject.add(item.id, new Gson().toJsonTree(profilePathJsonObject));
        }

        try (FileWriter fileWriter = new FileWriter(Tools.FILE_PROFILE_PATH)) {
            new Gson().toJson(jsonObject, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
