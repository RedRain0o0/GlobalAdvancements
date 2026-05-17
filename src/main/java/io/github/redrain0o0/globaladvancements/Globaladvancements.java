package io.github.redrain0o0.globaladvancements;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Globaladvancements implements ModInitializer {
    public static final String MOD_ID = "globaladvancements";
    public static final String MOD_NAME = "Global Advancements";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
    }

    public static Identifier createId(String id) {
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }
}
