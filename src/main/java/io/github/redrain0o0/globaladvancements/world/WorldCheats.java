package io.github.redrain0o0.globaladvancements.world;

import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.WorldData;

public interface WorldCheats {
    String TAG = "globaladvancements:cheated";

    boolean hasCheated();

    void markCheated();

    static boolean isCheated(LevelSettings settings) {
        return ((WorldCheats) (Object) settings).hasCheated();
    }

    static boolean isCheated(LevelSummary summary) {
        return isCheated(summary.getSettings());
    }

    static boolean isCheated(WorldData data) {
        return isCheated(data.getLevelSettings());
    }
}
