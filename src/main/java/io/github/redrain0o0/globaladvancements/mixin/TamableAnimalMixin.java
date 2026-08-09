package io.github.redrain0o0.globaladvancements.mixin;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.criterion.CriterionEventTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin {
    @Inject(method = "tame", at = @At("TAIL"))
    private void gadva$tamedWolf(Player player, CallbackInfo ci) {
        TamableAnimal animal = (TamableAnimal) (Object) this;
        if (animal instanceof Wolf wolf && player instanceof ServerPlayer serverPlayer) {
            Globaladvancements.sendCriterionEvent(
                    serverPlayer,
                    CriterionEventTypes.TAME_WOLF,
                    BuiltInRegistries.ENTITY_TYPE.getKey(wolf.getType())
            );
        }
    }
}
