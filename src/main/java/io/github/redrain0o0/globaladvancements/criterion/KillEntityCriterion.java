package io.github.redrain0o0.globaladvancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class KillEntityCriterion extends SimpleCriterionTrigger<KillEntityCriterion.TriggerInstance> {
    public void initialize() {
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity, damageSource) ->
                getPlayer(entity, damageSource).ifPresent((player) -> this.trigger(player, killedEntity))
        );
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, LivingEntity killedEntity) {
        this.trigger(player, (triggerInstance) -> triggerInstance.matches(killedEntity));
    }

    private static Optional<ServerPlayer> getPlayer(Entity entity, DamageSource damageSource) {
        if (damageSource.getEntity() instanceof ServerPlayer player) {
            return Optional.of(player);
        }

        if (entity instanceof ServerPlayer player) {
            return Optional.of(player);
        }

        return Optional.empty();
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<String> entity, Optional<String> category) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create((i) -> i.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.optionalFieldOf("entity").forGetter(TriggerInstance::entity),
                Codec.STRING.optionalFieldOf("category").forGetter(TriggerInstance::category)
        ).apply(i, TriggerInstance::new));

        public boolean matches(LivingEntity killedEntity) {
            return matchesEntity(killedEntity) && matchesCategory(killedEntity);
        }

        private boolean matchesEntity(LivingEntity killedEntity) {
            if (entity.isEmpty()) {
                return true;
            }

            String entityId = entity.get();
            if (entityId.startsWith("#")) {
                TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse(entityId.substring(1)));
                return killedEntity.getType().builtInRegistryHolder().is(tag);
            }

            return EntityType.getKey(killedEntity.getType()).equals(Identifier.parse(entityId));
        }

        private boolean matchesCategory(LivingEntity killedEntity) {
            return category.isEmpty() || killedEntity.getType().getCategory().getSerializedName().equals(category.get());
        }
    }
}
