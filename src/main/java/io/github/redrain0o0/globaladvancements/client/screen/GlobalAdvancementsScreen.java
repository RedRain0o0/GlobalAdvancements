package io.github.redrain0o0.globaladvancements.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientAdvancementView;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientProgressManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class GlobalAdvancementsScreen extends AdvancementsScreen {
    private final ClientAdvancementView advancements;
    private final Screen lastScreen;
    private Button resetButton;

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
    protected void init() {
        super.init();
        this.resetButton = this.addRenderableWidget(Button.builder(Component.translatable("gui.globaladvancements.reset"), button -> this.onResetPressed())
                .bounds(this.width - 55, this.height - 26, 50, 20)
                .tooltip(Tooltip.create(Component.translatable("gui.globaladvancements.reset.tooltip")))
                .build());
    }

    @Override
    protected void repositionElements() {
        super.repositionElements();
        if (this.resetButton != null) {
            this.resetButton.setX(this.width - 55);
            this.resetButton.setY(this.height - 26);
        }
    }

    private void onResetPressed() {
        if (InputConstants.isKeyDown(this.minecraft.getWindow(), InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(this.minecraft.getWindow(), InputConstants.KEY_RSHIFT)) {
            if (ClientProgressManager.restoreBackup()) {
                this.openFreshScreen();
            } else {
                boolean hasBackup = ClientProgressManager.hasBackup();
                this.minecraft.setScreen(new AlertScreen(this::openFreshScreen,
                        Component.translatable(hasBackup ? "gui.globaladvancements.restore.failed.title" : "gui.globaladvancements.restore.missing.title"),
                        Component.translatable(hasBackup ? "gui.globaladvancements.restore.failed.message" : "gui.globaladvancements.restore.missing.message"),
                        CommonComponents.GUI_BACK, false));
            }
            return;
        }

        this.minecraft.setScreen(new ConfirmScreen(confirmed -> {
            if (!confirmed) {
                this.openFreshScreen();
            } else if (ClientProgressManager.resetWithBackup()) {
                this.openFreshScreen();
            } else {
                this.minecraft.setScreen(new AlertScreen(this::openFreshScreen,
                        Component.translatable("gui.globaladvancements.reset.failed.title"),
                        Component.translatable("gui.globaladvancements.reset.failed.message"),
                        CommonComponents.GUI_BACK, false));
            }
        }, Component.translatable("gui.globaladvancements.reset.title"),
                Component.translatable("gui.globaladvancements.reset.message"),
                Component.translatable("gui.globaladvancements.reset"), CommonComponents.GUI_CANCEL));
    }

    private void openFreshScreen() {
        this.minecraft.setScreen(new GlobalAdvancementsScreen(this.lastScreen));
    }

    @Override
    public void removed() {
        this.advancements.setListener(null);
    }
}
