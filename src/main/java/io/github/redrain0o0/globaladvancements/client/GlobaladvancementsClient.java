package io.github.redrain0o0.globaladvancements.client;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientAdvancementManager;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientCriterionManager;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientProgressManager;
import io.github.redrain0o0.globaladvancements.client.criterion.OpenInventoryCriterionClient;
import io.github.redrain0o0.globaladvancements.network.ClientboundCriterionEventPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;

import java.io.File;
import java.io.IOException;

public class GlobaladvancementsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(ClientAdvancementManager.INSTANCE);

        fileInitializer(GACFile.ADVANCEMENTS_FILE);
        fileInitializer(GACFile.CONFIG_FILE);
        ClientProgressManager.load();
        ClientCriterionManager.initialize();
        OpenInventoryCriterionClient.initialize();

        ClientPlayNetworking.registerGlobalReceiver(ClientboundCriterionEventPayload.TYPE, (payload, context) ->
                context.client().execute(() -> ClientCriterionManager.trigger(payload.trigger(), payload.value()))
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
