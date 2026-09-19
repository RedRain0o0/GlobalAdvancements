package io.github.redrain0o0.globaladvancements.client;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientAdvancementManager;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientCriterionManager;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientProgressManager;
import io.github.redrain0o0.globaladvancements.client.criterion.OpenInventoryCriterionClient;
import io.github.redrain0o0.globaladvancements.network.*;
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
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(ClientAdvancementManager.INSTANCE);

        fileInitializer(GACFile.ADVANCEMENTS_FILE);
        fileInitializer(GACFile.CONFIG_FILE);
        ClientProgressManager.load();
        ClientCriterionManager.initialize();
        OpenInventoryCriterionClient.initialize();

        ClientPlayNetworking.registerGlobalReceiver(ClientboundModCheckPayload.TYPE, (payload, context) -> Globaladvancements.LOGGER.info("The ModCheck Packet should never be sent, only used for canSend"));
        ClientPlayNetworking.registerGlobalReceiver(ClientboundAdvancementHolderIdPayload.TYPE, (payload, context) -> {
            boolean hadAdvancement = ClientAdvancementManager.get(payload.advancementHolderId())
                    .map(ClientProgressManager::isComplete)
                    .orElse(false);
            boolean hasAdvancement = ClientAdvancementManager.get(payload.advancementHolderId())
                    .map((advancement) -> {
                        for (String criterion : payload.criterion()) ClientProgressManager.completeCriterion(advancement, criterion);
                        return ClientProgressManager.isComplete(advancement);
                    })
                    .orElse(false);
            ClientPlayNetworking.send(new ServerboundVerifyAdvancementPayload(payload.advancementHolderId(), hasAdvancement, hadAdvancement));
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundCriterionEventPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientCriterionManager.trigger(payload.trigger(), payload.value()))
        );
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientAdvancementManager.loadVanillaAdvancements(handler.registryAccess());
            serverHasMod = ClientPlayNetworking.canSend(ServerboundModCheckPayload.TYPE);
            Globaladvancements.LOGGER.info(serverHasMod ? "Server has Global Advancements installed" : "Server doesn't have Global Advancements installed");
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) ->
                ClientCriterionManager.clearMinecraftAdvancements()
        );
    }

    private static void fileInitializer(GACFile file) {
        try {
            File target = new File(file.getPath());
            if (target.createNewFile() && file == GACFile.ADVANCEMENTS_FILE) {
                Globaladvancements.LOGGER.info("Write default advancements file");
                ClientProgressManager.save();
            }
        } catch (IOException exception) {
            Globaladvancements.LOGGER.error(exception.getMessage());
        }
    }

    public static boolean serverHasMod() {
        return serverHasMod;
    }

    private enum GACFile {
        ADVANCEMENTS_FILE("/advancements.json"),
        CONFIG_FILE("/config/globaladvancements.json");

        private final String path;

        GACFile(String path) {
            this.path = Minecraft.getInstance().gameDirectory.getAbsolutePath() + path;
        }

        public String getPath() {
            return path;
        }
    }
}
