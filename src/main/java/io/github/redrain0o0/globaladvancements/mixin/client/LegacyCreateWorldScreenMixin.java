package io.github.redrain0o0.globaladvancements.mixin.client;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wily.legacy.client.screen.ConfirmationScreen;

@Mixin(CreateWorldScreen.class)
public abstract class LegacyCreateWorldScreenMixin extends Screen {
    @Shadow
    public abstract void onCreate();

    @Shadow
    @Final
    private WorldCreationUiState uiState;

    protected LegacyCreateWorldScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "onCreate", at = @At("HEAD"), cancellable = true)
    private void gadva$confirmCheats(CallbackInfo ci) {
        boolean isCreative = this.uiState.getGameMode() == WorldCreationUiState.SelectedGameMode.CREATIVE;
        if ((!Globaladvancements.confirmThatIWantToCreateOrJoinTheWorldEvenThoughIHaveCheatsEnabled) && (this.uiState.isAllowCommands() || isCreative)) {
            ci.cancel();
            this.minecraft.setScreen(new ConfirmationScreen(this, Component.translatable("gui.globaladvancements.startGame"), Component.translatable(isCreative ? "gui.globaladvancements.creativeMode" : "gui.globaladvancements.cheats"), (_) -> {
                Globaladvancements.confirmThatIWantToCreateOrJoinTheWorldEvenThoughIHaveCheatsEnabled = true;
                this.onCreate();
            }));
        }
    }

    @Inject(method = "onCreate", at = @At("TAIL"))
    private void gadva$resetCheats(CallbackInfo ci) {
        Globaladvancements.confirmThatIWantToCreateOrJoinTheWorldEvenThoughIHaveCheatsEnabled = false;
    }
}
