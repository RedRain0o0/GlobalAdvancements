package io.github.redrain0o0.globaladvancements.mixin.client;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.world.WorldCheats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldListEntryMixin {
    @Shadow private @Final LevelSummary summary;
    @Shadow private @Final Minecraft minecraft;

    @Shadow
    public abstract void joinWorld();

    @Shadow
    @Final
    private Screen screen;

    @Inject(method = "joinWorld", at = @At("HEAD"), cancellable = true)
    private void gadva$advancementPopup(CallbackInfo ci) {
        boolean isCreative = this.summary.getGameMode() == GameType.CREATIVE;
        if ((!Globaladvancements.confirmThatIWantToCreateOrJoinTheWorldEvenThoughIHaveCheatsEnabled) && WorldCheats.isCheated(this.summary)) {
            ci.cancel();

            this.minecraft.setScreen(new ConfirmScreen((result) -> {
                if (result) {
                    Globaladvancements.confirmThatIWantToCreateOrJoinTheWorldEvenThoughIHaveCheatsEnabled = true;
                    this.joinWorld();
                } else this.minecraft.setScreen(this.screen);
            }, Component.translatable("gui.globaladvancements.createWorld"), Component.translatable(isCreative ? "gui.globaladvancements.creativeMode" : this.summary.hasCommands() ? "gui.globaladvancements.cheats" : "gui.globaladvancements.priorCheats"), CommonComponents.GUI_OK, CommonComponents.GUI_CANCEL));
        }
    }

    @Inject(method = "joinWorld", at = @At("TAIL"))
    private void gadva$resetCheats(CallbackInfo ci) {
        Globaladvancements.confirmThatIWantToCreateOrJoinTheWorldEvenThoughIHaveCheatsEnabled = false;
    }
}
