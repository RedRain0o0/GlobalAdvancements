package io.github.redrain0o0.globaladvancements.client.screen;

import io.github.redrain0o0.globaladvancements.client.advancements.ClientAdvancementView;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class GlobalAdvancementsScreen extends AdvancementsScreen {
    private final ClientAdvancementView advancements;
    private final Screen lastScreen;

    public GlobalAdvancementsScreen(@Nullable Screen lastScreen) {
        this(lastScreen, ClientAdvancementView.create());
    }

    private GlobalAdvancementsScreen(@Nullable Screen lastScreen, ClientAdvancementView advancements) {
        super(advancements, lastScreen);
        this.advancements = advancements;
        this.lastScreen = lastScreen;
    }

    public static void refreshIfOpen() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof GlobalAdvancementsScreen screen) {
            minecraft.setScreen(new GlobalAdvancementsScreen(screen.lastScreen));
        }
    }

    @Override
    public void removed() {
        this.advancements.setListener(null);
    }
}
