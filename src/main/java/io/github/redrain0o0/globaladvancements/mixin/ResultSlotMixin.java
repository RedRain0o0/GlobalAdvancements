package io.github.redrain0o0.globaladvancements.mixin;

import io.github.redrain0o0.globaladvancements.criterion.GACriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ResultSlot.class)
public class ResultSlotMixin {
    @Inject(method = "onTake", at = @At("HEAD"))
    private void gadva$craftedItem(Player player, ItemStack stack, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            GACriteriaTriggers.CRAFT_ITEM.trigger(serverPlayer, stack);
        }
    }
}
