package io.github.redrain0o0.globaladvancements.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Debug(export = true)
@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {
    @Shadow
    private ServerPlayer player;

    @Inject(method = "award", at = @At("HEAD"))
    private void gadva$criterion(AdvancementHolder holder, String criterion, CallbackInfoReturnable<Boolean> cir) {
        if (this.player.getAttachedOrElse(Globaladvancements.HAS_MOD, false)) Globaladvancements.LOGGER.info("{} progressed {} with criterion {}", player.getName(), holder.id(), criterion);
        if (Globaladvancements.criterion.get(player.getUUID()) == null) { // Player Not Accounted
            Map<Identifier, List<String>> advancement = new HashMap<>();
            List<String> criterionList = new ArrayList<>();
            criterionList.add(criterion);
            advancement.put(holder.id(), criterionList);
            Globaladvancements.criterion.put(player.getUUID(), advancement);
        } else if (Globaladvancements.criterion.get(player.getUUID()).get(holder.id()) == null) { // Advancement Not Accounted
            List<String> criterionList = new ArrayList<>();
            criterionList.add(criterion);
            Globaladvancements.criterion.get(player.getUUID()).put(holder.id(), criterionList);
        } else { // Criterion Not Accounted
            List<String> oldCriterionList = Globaladvancements.criterion.get(player.getUUID()).get(holder.id());
            List<String> newCriterionList = oldCriterionList;
            newCriterionList.add(criterion);
            Globaladvancements.criterion.get(player.getUUID()).replace(holder.id(), oldCriterionList, newCriterionList);
        }
    }

    @ModifyExpressionValue(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/AdvancementProgress;isDone()Z", ordinal = 1))
    private boolean gadva$cancelAwardVisuals(boolean original, AdvancementHolder advancementHolder, String criterion) {
        boolean playerHasMod = this.player.getAttachedOrElse(Globaladvancements.HAS_MOD, false);
        // TODO: Move this code to gadva$criterion, as that is where we decide if the player has completed their advancement
        //if (original && playerHasMod) {
        //    Globaladvancements.LOGGER.info("{} progressed {} with criterion {}", player.getName(), advancementHolder.id(), criterion);
        //    Globaladvancements.queriedAdvancements.add(new Pair<>(player.getUUID(), advancementHolder.id()));
        //    ServerPlayNetworking.send(player, new ClientboundAdvancementHolderIdPayload(advancementHolder.id()/*, criterion*/));
        //}
        return original && !playerHasMod;
    }
}
