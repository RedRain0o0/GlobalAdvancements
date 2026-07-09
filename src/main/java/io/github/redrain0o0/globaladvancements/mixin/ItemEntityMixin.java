package io.github.redrain0o0.globaladvancements.mixin;

import io.github.redrain0o0.globaladvancements.criterion.GACriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Unique
    private ItemStack gadva$pickupStack = ItemStack.EMPTY;

    @Shadow
    public abstract ItemStack getItem();

    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void gadva$storePickupStack(Player player, CallbackInfo ci) {
        this.gadva$pickupStack = this.getItem().copy();
    }

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;take(Lnet/minecraft/world/entity/Entity;I)V"))
    private void gadva$pickedUpItem(Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer && !this.gadva$pickupStack.isEmpty()) {
            GACriteriaTriggers.PICKUP_ITEM.trigger(serverPlayer, this.gadva$pickupStack);
        }
    }
}
