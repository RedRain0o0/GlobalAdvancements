package io.github.redrain0o0.globaladvancements.mixin.client;

import com.bawnorton.mixinsquared.TargetHandler;
import io.github.redrain0o0.globaladvancements.client.screen.LegacyGlobalAdvancementsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wily.legacy.client.LegacyOptions;

@Mixin(value = Minecraft.class, priority = 1500)
public class LegacyMinecraftMixin {
    @TargetHandler(mixin = "wily.legacy.mixin.base.client.MinecraftMixin", name = "handleKeybinds(Lnet/minecraft/client/gui/screens/Screen;)Lnet/minecraft/client/gui/screens/Screen;")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void gadva$clientAdvancements(Screen screen, CallbackInfoReturnable<Screen> cir) {
        if (LegacyOptions.legacyAdvancements.get()) {
            cir.setReturnValue(LegacyGlobalAdvancementsScreen.getActualAdvancementsScreenInstance(null));
        }
    }
}
