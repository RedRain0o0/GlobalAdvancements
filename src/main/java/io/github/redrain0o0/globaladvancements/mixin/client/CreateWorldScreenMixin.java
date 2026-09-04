package io.github.redrain0o0.globaladvancements.mixin.client;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {
    @Shadow
    public abstract void onCreate();

    @Shadow
    @Final
    private WorldCreationUiState uiState;

    protected CreateWorldScreenMixin(Component title) {
        super(title);
    }
    //@Inject(method = "lambda$init$0", at = @At("HEAD"), cancellable = true)
    //private void gadva$confirmCheats(Button button, CallbackInfo ci) {
    //    Globaladvancements.LOGGER.info("TEST");
    //}

    @Inject(method = "onCreate", at = @At("HEAD"), cancellable = true)
    private void gadva$confirmCheats(CallbackInfo ci) {
        boolean isCreative = this.uiState.getGameMode() == WorldCreationUiState.SelectedGameMode.CREATIVE;
        if ((!Globaladvancements.confirmThatIWantToCreateTheWorldEvenThoughIHaveCheatsEnabled) && (this.uiState.isAllowCommands() || isCreative)) {
            ci.cancel();


            this.minecraft.setScreen(new ConfirmScreen((result) -> {
                if (result) {
                    Globaladvancements.confirmThatIWantToCreateTheWorldEvenThoughIHaveCheatsEnabled = true;
                    this.onCreate();
                }

                this.minecraft.setScreen(this);
            }, Component.translatable("gui.globaladvancements.createWorld"), Component.translatable(isCreative ? "gui.globaladvancements.creativeMode" : "gui.globaladvancements.cheats"), CommonComponents.GUI_OK, CommonComponents.GUI_CANCEL));

            //this.minecraft.setScreen(new CheatsPopupScreen((CreateWorldScreen) (Object) this));
            Globaladvancements.LOGGER.info("TEST 2");
        }
    }

    @Inject(method = "onCreate", at = @At("TAIL"), cancellable = true)
    private void gadva$resetCheats(CallbackInfo ci) {
        Globaladvancements.confirmThatIWantToCreateTheWorldEvenThoughIHaveCheatsEnabled = false;
    }
}
