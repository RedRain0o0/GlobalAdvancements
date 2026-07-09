package io.github.redrain0o0.globaladvancements.mixin;

import io.github.redrain0o0.globaladvancements.criterion.GACriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceResultSlot.class)
public class FurnaceResultSlotMixin {
    @Inject(method = "onTake", at = @At("HEAD"))
    private void gadva$smeltedItem(Player player, ItemStack stack, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            GACriteriaTriggers.SMELT_ITEM.trigger(serverPlayer, stack);
        }
    }
}
