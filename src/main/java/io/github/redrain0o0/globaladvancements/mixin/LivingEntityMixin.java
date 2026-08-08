package io.github.redrain0o0.globaladvancements.mixin;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.criterion.CriterionEventTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "causeFallDamage", at = @At("RETURN"))
    private void gadva$fallFromVehicle(double distance, float damageMultiplier, DamageSource damageSource,
                                       CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof ServerPlayer player
                && player.getVehicle() instanceof Pig pig
                && damageSource.is(DamageTypeTags.IS_FALL)
                && cir.getReturnValue()) {
            Globaladvancements.sendCriterionEvent(
                    player,
                    CriterionEventTypes.FALL_FROM_VEHICLE,
                    BuiltInRegistries.ENTITY_TYPE.getKey(pig.getType())
            );
        }
    }
}
