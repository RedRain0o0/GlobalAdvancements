package io.github.redrain0o0.globaladvancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class MineBlockCriterion extends SimpleCriterionTrigger<MineBlockCriterion.TriggerInstance> {
    public void initialize() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                this.trigger(serverPlayer, state);
            }
        });
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockState state) {
        this.trigger(player, (triggerInstance) -> triggerInstance.matches(state));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<String> blocks) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create((i) -> i.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.optionalFieldOf("blocks").forGetter(TriggerInstance::blocks)
        ).apply(i, TriggerInstance::new));

        public boolean matches(BlockState state) {
            return blocks.isEmpty() || matchesBlock(state, blocks.get());
        }

        private boolean matchesBlock(BlockState state, String blockId) {
            if (blockId.startsWith("#")) {
                TagKey<Block> tag = TagKey.create(Registries.BLOCK, Identifier.parse(blockId.substring(1)));
                return state.is(tag, (blockState) -> true);
            }

            return BuiltInRegistries.BLOCK.getKey(state.getBlock()).equals(Identifier.parse(blockId));
        }
    }
}
