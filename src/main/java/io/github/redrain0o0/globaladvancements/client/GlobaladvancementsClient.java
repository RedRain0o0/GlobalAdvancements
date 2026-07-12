package io.github.redrain0o0.globaladvancements.client;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientAdvancementManager;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientCriterionManager;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientProgressManager;
import io.github.redrain0o0.globaladvancements.client.criterion.OpenInventoryCriterionClient;
import io.github.redrain0o0.globaladvancements.network.ClientboundAdvancementHolderIdPayload;
import io.github.redrain0o0.globaladvancements.network.ClientboundModCheckPayload;
import io.github.redrain0o0.globaladvancements.network.ServerboundCriterionMappingsPayload;
import io.github.redrain0o0.globaladvancements.network.ServerboundModCheckPayload;
import io.github.redrain0o0.globaladvancements.network.ServerboundVerifyAdvancementPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;

import java.io.File;
import java.io.IOException;

public class GlobaladvancementsClient implements ClientModInitializer {
    private static boolean serverHasMod = false;

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(ClientCriterionManager.INSTANCE);
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(ClientAdvancementManager.INSTANCE);

        fileInitializer(GACFile.ADVANCEMENTS_FILE);
        //fileInitializer(GACFile.STATS_FILE);
        fileInitializer(GACFile.CONFIG_FILE);
        ClientProgressManager.load();
        OpenInventoryCriterionClient.initialize();

        ClientPlayConnectionEvents.JOIN.register((clientPacketListener, packetSender, minecraft) -> {
            serverHasMod = ClientPlayNetworking.canSend(ServerboundModCheckPayload.TYPE);
            Globaladvancements.LOGGER.info(serverHasMod ? "Server has "+Globaladvancements.MOD_NAME+" installed" : "Server doesn't have "+Globaladvancements.MOD_NAME+" installed");
            if (serverHasMod) {
                ClientPlayNetworking.send(new ServerboundCriterionMappingsPayload(ClientAdvancementManager.criterionMappings()));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundModCheckPayload.TYPE, (payload, context) -> Globaladvancements.LOGGER.info("The ModCheck Packet should never be sent, only used for canSend"));
        ClientPlayNetworking.registerGlobalReceiver(ClientboundAdvancementHolderIdPayload.TYPE, (payload, context) -> {
            Globaladvancements.LOGGER.info("Server asked if we have '{}'", payload.advancementHolderId());
            boolean hadAdvancement = ClientAdvancementManager.get(payload.advancementHolderId())
                    .map(ClientProgressManager::isComplete)
                    .orElse(false);
            boolean hasAdvancement = ClientAdvancementManager.get(payload.advancementHolderId())
                    .map((advancement) -> {
                        ClientProgressManager.completeCriterion(advancement, payload.criterion());
                        return ClientProgressManager.isComplete(advancement);
                    })
                    .orElse(false);
            Globaladvancements.LOGGER.info("{}, {}", hadAdvancement, hasAdvancement);
            ClientPlayNetworking.send(new ServerboundVerifyAdvancementPayload(payload.advancementHolderId(), hasAdvancement, hadAdvancement));
        });
    }

    private static void fileInitializer(GACFile file) {
        try {
            File file1 = new File(file.getPath());
            if (file1.createNewFile()) {
                switch (file) {
                    case ADVANCEMENTS_FILE -> {
                        Globaladvancements.LOGGER.info("Write default advancements file");
                        ClientProgressManager.save();
                    }
                    case STATS_FILE -> {} // Unused, reevaluate once creepers done his stats thing
                    case CONFIG_FILE -> {
                        Globaladvancements.LOGGER.info("Write default config file");
                    }
                }
            }
        } catch (IOException e) {
            Globaladvancements.LOGGER.error(e.getMessage());
        }
    }

    private enum GACFile {
        ADVANCEMENTS_FILE("/advancements.json"),
        STATS_FILE("/stats.json"),
        CONFIG_FILE("/config/globaladvancements.json");

        private final String path;


        GACFile(String path) {
            this.path = Minecraft.getInstance().gameDirectory.getAbsolutePath()+path;

        }

        public String getPath() {
            return path;
        }

    }

    public static boolean doesServerHaveMod() {
        return serverHasMod;
    }
}
