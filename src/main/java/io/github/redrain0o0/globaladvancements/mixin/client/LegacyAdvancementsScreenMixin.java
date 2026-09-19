package io.github.redrain0o0.globaladvancements.mixin.client;

import io.github.redrain0o0.globaladvancements.client.GlobaladvancementsClient;
import io.github.redrain0o0.globaladvancements.client.screen.LegacyGlobalAdvancementsScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wily.legacy.client.screen.LegacyAdvancementsScreen;

@Mixin(LegacyAdvancementsScreen.class)
public abstract class LegacyAdvancementsScreenMixin {
    @Inject(method = "getActualAdvancementsScreenInstance", at = @At("HEAD"), cancellable = true)
    private static void gadva$createScreen(Screen parent, CallbackInfoReturnable<Screen> cir) {
        if (!GlobaladvancementsClient.serverHasMod()) return;
        cir.setReturnValue(LegacyGlobalAdvancementsScreen.getActualAdvancementsScreenInstance(parent));
    }
}
