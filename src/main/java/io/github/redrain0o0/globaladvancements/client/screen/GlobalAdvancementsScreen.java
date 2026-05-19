package io.github.redrain0o0.globaladvancements.client.screen;

import com.google.gson.JsonObject;
import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Environment(EnvType.CLIENT)
public class GlobalAdvancementsScreen extends Screen {
    private static final Identifier WINDOW_LOCATION = Identifier.withDefaultNamespace("textures/gui/advancements/window.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final int WINDOW_INSIDE_X = 9;
    private static final int WINDOW_INSIDE_Y = 18;
    public static final int WINDOW_INSIDE_WIDTH = 234;
    public static final int WINDOW_INSIDE_HEIGHT = 113;
    private static final int WINDOW_TITLE_X = 8;
    private static final int WINDOW_TITLE_Y = 6;
    private static final int BACKGROUND_TEXTURE_WIDTH = 256;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 256;
    public static final int BACKGROUND_TILE_WIDTH = 16;
    public static final int BACKGROUND_TILE_HEIGHT = 16;
    public static final int BACKGROUND_TILE_COUNT_X = 14;
    public static final int BACKGROUND_TILE_COUNT_Y = 7;
    private static final double SCROLL_SPEED = (double)16.0F;
    private static final Component VERY_SAD_LABEL = Component.translatable("advancements.sad_label");
    private static final Component NO_ADVANCEMENTS_LABEL = Component.translatable("advancements.empty");
    private static final Component TITLE = Component.translatable("gui.advancements");
    private final HeaderAndFooterLayout layout;
    @Nullable
    private final Screen lastScreen;

    public GlobalAdvancementsScreen(@Nullable Screen screen) {
        super(TITLE);
        this.layout = new HeaderAndFooterLayout(this);
        this.lastScreen = screen;
    }

    protected void init() {
        //JsonObject advancementsJson = JsonFileReader.readJson("advancements");
        //if (!advancementsJson.getAsJsonArray("unlocked_advancements").isEmpty()) {
        //    Globaladvancements.LOGGER.info("{}", advancementsJson.get("unlocked_advancements"));
        //} else {
        //    Globaladvancements.LOGGER.info("No advancements currently unlocked");
        //}

        this.layout.addTitleHeader(TITLE, this.font);

        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, (button) -> this.onClose()).width(200).build());
        this.layout.visitWidgets((guiEventListener) -> {
            AbstractWidget var10000 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    protected void repositionElements() {
        this.layout.arrangeElements();
    }

    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    public boolean keyPressed(KeyEvent keyEvent) {
        if (this.minecraft.level == null) return super.keyPressed(keyEvent);
        if (this.minecraft.options.keyAdvancements.matches(keyEvent)) {
            this.minecraft.setScreen((Screen)null);
            this.minecraft.mouseHandler.grabMouse();
            return true;
        } else {
            return super.keyPressed(keyEvent);
        }
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int i, int j, float f) {
        super.extractRenderState(guiGraphics, i, j, f);
        guiGraphics.text(this.font, "ClientAdvancementsScreen", 1, 1, 0xFFFFFFFF);
        int k = (this.width - 252) / 2;
        int l = (this.height - 140) / 2;
        guiGraphics.nextStratum();
        this.renderInside(guiGraphics, k, l);
        guiGraphics.nextStratum();
        this.renderWindow(guiGraphics, k, l);
        this.renderTooltips(guiGraphics, i, j, k, l);
    }

    private void renderInside(GuiGraphicsExtractor guiGraphics, int i, int j) {
        guiGraphics.fill(i + 9, j + 18, i + 9 + 234, j + 18 + 113, -16777216);
        int k = i + 9 + 117;
        Font font = this.font;
        Component messageComponent = NO_ADVANCEMENTS_LABEL;
        int textPos = j + 18 + 56;
        Objects.requireNonNull(this.font);
        guiGraphics.centeredText(font, messageComponent, k, textPos - 9 / 2, -1);
        font = this.font;
        messageComponent = VERY_SAD_LABEL;
        textPos = j + 18 + 113;
        Objects.requireNonNull(this.font);
        guiGraphics.centeredText(font, messageComponent, k, textPos - 9, -1);
    }

    public void renderWindow(GuiGraphicsExtractor guiGraphics, int i, int j) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, WINDOW_LOCATION, i, j, 0.0F, 0.0F, 252, 140, 256, 256);
        /*if (this.tabs.size() > 1) {
            for(AdvancementTab advancementTab : this.tabs.values()) {
                advancementTab.drawTab(guiGraphics, i, j, advancementTab == this.selectedTab);
            }

            for(AdvancementTab advancementTab : this.tabs.values()) {
                advancementTab.drawIcon(guiGraphics, i, j);
            }
        }*/

        //guiGraphics.drawString(this.font, this.selectedTab != null ? this.selectedTab.getTitle() : TITLE, i + 8, j + 6, -12566464, false);
        guiGraphics.text(this.font, TITLE, i + 8, j + 6, -12566464, false);
    }

    private void renderTooltips(GuiGraphicsExtractor guiGraphics, int i, int j, int k, int l) {
        //if (this.selectedTab != null) {
        //    guiGraphics.pose().pushMatrix();
        //    guiGraphics.pose().translate((float)(k + 9), (float)(l + 18));
        //    guiGraphics.nextStratum();
        //    this.selectedTab.drawTooltips(guiGraphics, i - k - 9, j - l - 18, k, l);
        //    guiGraphics.pose().popMatrix();
        //}

        //if (this.tabs.size() > 1) {
        //    for(AdvancementTab advancementTab : this.tabs.values()) {
        //        if (advancementTab.isMouseOver(k, l, (double)i, (double)j)) {
        //            guiGraphics.setTooltipForNextFrame(this.font, advancementTab.getTitle(), i, j);
        //        }
        //    }
        //}
    }
}