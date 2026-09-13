package io.github.redrain0o0.globaladvancements.mixin.client;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShareToLanScreen.class)
public abstract class ShareToLanScreenMixin extends Screen {
    @Shadow private boolean commands;
    @Shadow private GameType gameMode;
    @Shadow protected abstract void lambda$init$2(IntegratedServer par1, Button par2);

    protected ShareToLanScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "lambda$init$2", at = @At("HEAD"), cancellable = true)
    private void gadva$advancementsPopup(IntegratedServer singleplayerServer, Button button, CallbackInfo ci) {
        if (!commands && !gameMode.isCreative()) return;
        if (Globaladvancements.confirmThatIWantToCreateOrJoinTheWorldEvenThoughIHaveCheatsEnabled) {
            Globaladvancements.confirmThatIWantToCreateOrJoinTheWorldEvenThoughIHaveCheatsEnabled = false;
            return;
        }
        ci.cancel();
        this.minecraft.setScreen(new ConfirmScreen((result) -> {
            if (result) {
                Globaladvancements.confirmThatIWantToCreateOrJoinTheWorldEvenThoughIHaveCheatsEnabled = true;
                this.lambda$init$2(singleplayerServer, button);
            } else this.minecraft.setScreen(this);
        }, Component.translatable("gui.globaladvancements.createWorld"), Component.translatable(gameMode.isCreative() ? "gui.globaladvancements.creativeMode" : "gui.globaladvancements.cheats"), CommonComponents.GUI_OK, CommonComponents.GUI_CANCEL));
    }
}
