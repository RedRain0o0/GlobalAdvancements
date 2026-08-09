package io.github.redrain0o0.globaladvancements;

import io.github.redrain0o0.globaladvancements.criterion.CriterionEventTypes;
import io.github.redrain0o0.globaladvancements.network.ClientboundCriterionEventPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Globaladvancements implements ModInitializer {
    public static final String MOD_ID = "globaladvancements";
    public static final String MOD_NAME = "Global Advancements";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(ClientboundCriterionEventPayload.TYPE, ClientboundCriterionEventPayload.STREAM_CODEC);
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer) {
                sendCriterionEvent(serverPlayer, CriterionEventTypes.MINE_BLOCK, BuiltInRegistries.BLOCK.getKey(state.getBlock()));
            }
        });
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity, damageSource) -> {
            ServerPlayer player = findPlayer(entity, damageSource);
            if (player != null) {
                sendCriterionEvent(player, CriterionEventTypes.KILL_ENTITY, BuiltInRegistries.ENTITY_TYPE.getKey(killedEntity.getType()));
                if (isSniperDuel(player, killedEntity, damageSource)) {
                    sendCriterionEvent(player, CriterionEventTypes.SNIPER_DUEL, BuiltInRegistries.ENTITY_TYPE.getKey(killedEntity.getType()));
                }
            }
        });
    }

    public static void sendCriterionEvent(ServerPlayer player, Identifier trigger, Identifier value) {
        if (ServerPlayNetworking.canSend(player, ClientboundCriterionEventPayload.TYPE)) {
            ServerPlayNetworking.send(player, new ClientboundCriterionEventPayload(trigger, value));
        }
    }

    private static ServerPlayer findPlayer(Entity entity, DamageSource damageSource) {
        if (damageSource.getEntity() instanceof ServerPlayer player) {
            return player;
        }
        if (entity instanceof ServerPlayer player) {
            return player;
        }
        return null;
    }

    private static boolean isSniperDuel(ServerPlayer player, Entity killedEntity, DamageSource damageSource) {
        if (killedEntity.getType() != EntityType.SKELETON || !(damageSource.getDirectEntity() instanceof Arrow)) {
            return false;
        }

        double x = player.getX() - killedEntity.getX();
        double z = player.getZ() - killedEntity.getZ();
        return x * x + z * z > 2500.0;
    }

    public static Identifier createId(String id) {
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }
}
