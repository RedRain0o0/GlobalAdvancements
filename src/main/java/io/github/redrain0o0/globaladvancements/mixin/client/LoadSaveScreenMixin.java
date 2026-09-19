package io.github.redrain0o0.globaladvancements.mixin.client;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.world.WorldCheats;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wily.legacy.client.screen.ConfirmationScreen;
import wily.legacy.client.screen.LoadSaveScreen;
import wily.legacy.client.screen.Panel;
import wily.legacy.client.screen.PanelBackgroundScreen;

@Mixin(LoadSaveScreen.class)
public abstract class LoadSaveScreenMixin extends PanelBackgroundScreen {
    @Shadow public abstract void onLoad();
    @Shadow public @Final LevelSummary summary;

    public LoadSaveScreenMixin(Screen parent, Panel.Constructor<PanelBackgroundScreen> panelConstructor, Component component) {
        super(parent, panelConstructor, component);
    }

    @Inject(method = "onLoad", at = @At("HEAD"), cancellable = true)
    private void gadva$confirmCheats(CallbackInfo ci) {
        boolean isCreative = this.summary.getGameMode() == GameType.CREATIVE;
        if ((!Globaladvancements.confirmThatIWantToCreateOrJoinTheWorldEvenThoughIHaveCheatsEnabled) && WorldCheats.isCheated(this.summary)) {
            ci.cancel();

            this.minecraft.setScreen(new ConfirmationScreen(this, Component.translatable("gui.globaladvancements.startGame"), Component.translatable(isCreative ? "gui.globaladvancements.creativeMode" : this.summary.hasCommands() ? "gui.globaladvancements.cheats" : "gui.globaladvancements.priorCheats"), (_) -> {
                Globaladvancements.confirmThatIWantToCreateOrJoinTheWorldEvenThoughIHaveCheatsEnabled = true;
                this.onLoad();
            }));
        }
    }

    @ModifyArg(method = "lambda$renderDefaultBackground$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;", ordinal = 0), index = 1)
    private Object[] gadva$hasCheats(Object[] args) {
        return new Component[]{(WorldCheats.isCheated(this.summary) ? GameType.CREATIVE : GameType.SURVIVAL).getShortDisplayName()};
    }
}
