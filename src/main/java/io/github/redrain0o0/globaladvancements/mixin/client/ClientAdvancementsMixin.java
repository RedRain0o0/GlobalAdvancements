package io.github.redrain0o0.globaladvancements.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientAdvancementManager;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientProgressManager;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientAdvancements.class)
public class ClientAdvancementsMixin {
    @ModifyExpressionValue(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/advancements/DisplayInfo;shouldShowToast()Z"
            )
    )
    private boolean gadva$showNativeToast(boolean original, @Local AdvancementNode advancement) {
        if (!original) {
            return false;
        }

        Identifier id = advancement.holder().id();
        return ClientAdvancementManager.get(id)
                .map(clientAdvancement -> ClientAdvancementManager.isVanilla(id)
                        && !ClientProgressManager.isComplete(clientAdvancement))
                .orElse(true);
    }
}
