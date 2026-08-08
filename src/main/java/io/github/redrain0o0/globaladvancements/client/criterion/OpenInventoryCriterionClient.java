package io.github.redrain0o0.globaladvancements.client.criterion;

import io.github.redrain0o0.globaladvancements.client.advancements.ClientCriterionManager;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public final class OpenInventoryCriterionClient {
    private OpenInventoryCriterionClient() {
    }

    public static void initialize() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof InventoryScreen) {
                ClientCriterionManager.trigger(ClientCriterionManager.OPEN_INVENTORY);
            }
        });
    }
}
