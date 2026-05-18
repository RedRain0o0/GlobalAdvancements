package io.github.redrain0o0.globaladvancements.client;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.network.ClientboundModCheckPayload;
import io.github.redrain0o0.globaladvancements.network.ServerboundModCheckPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;

public class GlobaladvancementsClient implements ClientModInitializer {
    private static final String ADVANCEMENTS_FILE = Minecraft.getInstance().gameDirectory.getAbsolutePath()+"/advancements.json";
    private static final String STATS_FILE = Minecraft.getInstance().gameDirectory.getAbsolutePath()+"/stats.json";
    private static final String CONFIG_FILE = Minecraft.getInstance().gameDirectory.getAbsolutePath()+"/config/globaladvancements.json";
    private static boolean serverHasMod = false;

    @Override
    public void onInitializeClient() {
        fileReadWriter(ADVANCEMENTS_FILE);
        fileReadWriter(STATS_FILE);
        fileReadWriter(CONFIG_FILE);

        ClientPlayConnectionEvents.JOIN.register((clientPacketListener, packetSender, minecraft) -> {
            serverHasMod = ClientPlayNetworking.canSend(ServerboundModCheckPayload.TYPE);
            Globaladvancements.LOGGER.info(serverHasMod ? "Server has "+Globaladvancements.MOD_NAME+" installed" : "Server doesn't have "+Globaladvancements.MOD_NAME+" installed");
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundModCheckPayload.TYPE, (payload, context) -> Globaladvancements.LOGGER.info("Test"));
    }

    private static void fileReadWriter(String file) {
        try {
            File file1 = new File(file);
            if (!file1.createNewFile()) {
                Globaladvancements.LOGGER.info("didn't Created new config file");
            } else {
                Globaladvancements.LOGGER.info("Created new config file");
            }
        } catch (IOException e) {
            Globaladvancements.LOGGER.error(e.getMessage());
        }
    }

    public static boolean doesServerHaveMod() {
        return serverHasMod;
    }
}
