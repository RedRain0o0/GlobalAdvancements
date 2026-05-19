package io.github.redrain0o0.globaladvancements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Pair;
import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.network.ClientboundAdvancementHolderIdPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementMixin {
    @Shadow
    private ServerPlayer player;

    // TODO: mixin for criterion unlocks

    @ModifyExpressionValue(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementProgress;isDone()Z", ordinal = 1))
    private boolean gadva$cancelAwardVisuals(boolean original, AdvancementHolder advancementHolder, String criterion) {
        boolean playerHasMod = this.player.getAttachedOrElse(Globaladvancements.HAS_MOD, false);
        if (original && playerHasMod) {
            Globaladvancements.LOGGER.info("{} progressed {} with criterion {}", player.getName(), advancementHolder.id(), criterion);
            Globaladvancements.queriedAdvancements.add(new Pair<>(player.getUUID(), advancementHolder.id()));
            ServerPlayNetworking.send(player, new ClientboundAdvancementHolderIdPayload(advancementHolder.id()/*, criterion*/));
        }
        return original && !playerHasMod;
    }
}
