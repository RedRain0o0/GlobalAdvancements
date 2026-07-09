package io.github.redrain0o0.globaladvancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class SmeltItemCriterion extends SimpleCriterionTrigger<SmeltItemCriterion.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack stack) {
        this.trigger(player, (triggerInstance) -> triggerInstance.matches(stack));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<String> result) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create((i) -> i.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.optionalFieldOf("result").forGetter(TriggerInstance::result)
        ).apply(i, TriggerInstance::new));

        public boolean matches(ItemStack stack) {
            return result.isEmpty() || BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(Identifier.parse(result.get()));
        }
    }
}
