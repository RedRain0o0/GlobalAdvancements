package io.github.redrain0o0.globaladvancements.mixin;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.criterion.CriterionEventTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({FlintAndSteelItem.class, FireChargeItem.class})
public abstract class PortalLightingItemMixin {
    @Inject(method = "useOn", at = @At("RETURN"))
    private void gadva$lightNetherPortal(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (!(context.getPlayer() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        BlockPos clickedPos = context.getClickedPos();
        BlockPos firePos = clickedPos.relative(context.getClickedFace());
        if (isNetherPortal(context.getLevel(), clickedPos) || isNetherPortal(context.getLevel(), firePos)) {
            Globaladvancements.sendCriterionEvent(
                    serverPlayer,
                    CriterionEventTypes.LIGHT_NETHER_PORTAL,
                    BuiltInRegistries.BLOCK.getKey(Blocks.NETHER_PORTAL)
            );
        }
    }

    private static boolean isNetherPortal(Level level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.NETHER_PORTAL);
    }
}
