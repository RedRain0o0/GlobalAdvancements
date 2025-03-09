package io.github.redrain0o0.globaladvancements;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojang.text2speech.Narrator.LOGGER;

public class GlobalAdvancements implements ModInitializer {

    public static final String MOD_ID = "globaladvancements";
    public static final Logger LOGGER = LoggerFactory.getLogger("Global Advancements");

    @Override
    public void onInitialize() {
        LOGGER.info("Initialising Global Advancements...");
    }
}
