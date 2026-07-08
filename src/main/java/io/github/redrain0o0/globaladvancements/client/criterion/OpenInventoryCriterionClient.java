package io.github.redrain0o0.globaladvancements.client.criterion;

import io.github.redrain0o0.globaladvancements.client.GlobaladvancementsClient;
import io.github.redrain0o0.globaladvancements.network.ServerboundOpenInventoryPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public class OpenInventoryCriterionClient {
    public static void initialize() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (GlobaladvancementsClient.doesServerHaveMod() && screen instanceof InventoryScreen && ClientPlayNetworking.canSend(ServerboundOpenInventoryPayload.TYPE)) {
                ClientPlayNetworking.send(new ServerboundOpenInventoryPayload(true));
            }
        });
    }
}
