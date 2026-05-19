package io.github.redrain0o0.globaladvancements.mixin.client;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.client.GlobaladvancementsClient;
import io.github.redrain0o0.globaladvancements.client.screen.GlobalAdvancementsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenMixin extends Screen {
    @Shadow private @Final @Nullable Screen lastScreen;

    protected AdvancementsScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void gadva$init(CallbackInfo ci) {
        if (GlobaladvancementsClient.doesServerHaveMod()) this.minecraft.setScreen(new GlobalAdvancementsScreen(this.lastScreen));
    }
}
