package io.github.redrain0o0.globaladvancements.client.mixins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    protected TitleScreenMixin (Component title) {
        super(title);
    }

    @Inject(at = @At("RETURN"), method = "init")
    private void init(CallbackInfo info) {
        this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), (button) -> {
            this.minecraft.setScreen(this);
        }).pos(1,1).size(20,120).build());
    }

    //@Inject(at = @At("RETURN"), method = "initWidgetsNormal")
    //private void method(int y, int spacingY, CallbackInfo ci) {
    //
    //}
    //@Inject(at = @At("RETURN"), method = )
    //private void addModsButton(int y, int spacingY, CallbackInfo ci) {
    //    this.addDrawableChild(Button.builder(Component.translatable("modname.customButton"), (button) -> {
    //        this.minecraft.setScreen(new SelectWorldScreen(this));
    //    }).pos(this.width / 2 - 200 +252, y).size(50, 200).build());
    //}

}