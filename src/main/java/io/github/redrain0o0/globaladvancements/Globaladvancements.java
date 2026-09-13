package io.github.redrain0o0.globaladvancements;

import com.mojang.datafixers.util.Pair;
import io.github.redrain0o0.globaladvancements.criterion.CriterionEventTypes;
import io.github.redrain0o0.globaladvancements.network.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Globaladvancements implements ModInitializer {
    public static final String MOD_ID = "globaladvancements";
    public static final String MOD_NAME = "Global Advancements";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final Map<UUID, Map<Identifier, List<String>>> criterion = new HashMap<>();
    public static final List<Pair<UUID, Identifier>> queriedAdvancements = new ArrayList<>();

    public static final AttachmentType<Boolean> HAS_MOD = AttachmentRegistry.create(createId("has_mod"));

    public static boolean confirmThatIWantToCreateOrJoinTheWorldEvenThoughIHaveCheatsEnabled = false; // i like doing stupidly long variable names for no reason :3c

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(ClientboundModCheckPayload.TYPE, ClientboundModCheckPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundAdvancementHolderIdPayload.TYPE, ClientboundAdvancementHolderIdPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundCriterionEventPayload.TYPE, ClientboundCriterionEventPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundModCheckPayload.TYPE, ServerboundModCheckPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundVerifyAdvancementPayload.TYPE, ServerboundVerifyAdvancementPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ServerboundModCheckPayload.TYPE, (payload, context) -> LOGGER.info("The ModCheck Packet should never be sent, only used for canSend"));

        ServerPlayNetworking.registerGlobalReceiver(ServerboundVerifyAdvancementPayload.TYPE, (payload, context) -> {
            Pair<UUID, Identifier> playerAdvancementPair = new Pair<>(context.player().getUUID(), payload.advancementHolderId());
            if (queriedAdvancements.contains(playerAdvancementPair)) {
                queriedAdvancements.remove(playerAdvancementPair);
                if (!payload.hasAdvancement()) return;
                if (payload.hadAdvancement()) return;
                ServerPlayer player = context.player();
                MinecraftServer server = player.level().getServer();

                AdvancementHolder holder = server.getAdvancements().get(payload.advancementHolderId());
                holder.value().rewards().grant(player);
                holder.value().display().ifPresent((display) -> {
                    if (display.shouldAnnounceChat() && player.level().getGameRules().get(GameRules.SHOW_ADVANCEMENT_MESSAGES)) {
                        server.getPlayerList().broadcastSystemMessage(display.getType().createAnnouncement(holder, player), false);
                    }
                });
            }
        });

        ServerPlayerEvents.JOIN.register((player) -> {
            boolean clientHasMod = ServerPlayNetworking.canSend(player, ClientboundModCheckPayload.TYPE);
            player.setAttached(HAS_MOD, clientHasMod);
            LOGGER.info(player.getName().getString() + (clientHasMod ? " has Global Advancements installed" : " doesn't have Global Advancements installed"));
        });

        ServerTickEvents.END_SERVER_TICK.register((minecraftServer) -> {
            if (criterion.isEmpty()) return;
            for (Map.Entry<UUID, Map<Identifier, List<String>>> playerPair : criterion.entrySet()) {
                ServerPlayer player = minecraftServer.getPlayerList().getPlayer(playerPair.getKey());
                if (player == null || !player.getAttachedOrElse(HAS_MOD, false)) continue;
                for (Map.Entry<Identifier, List<String>> advancementPair : playerPair.getValue().entrySet()) {
                    queriedAdvancements.add(new Pair<>(player.getUUID(), advancementPair.getKey()));
                    ServerPlayNetworking.send(player, new ClientboundAdvancementHolderIdPayload(advancementPair.getKey(), advancementPair.getValue()));
                }
            }
            criterion.clear();
        });

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
        if (damageSource.getEntity() instanceof ServerPlayer player) return player;
        if (entity instanceof ServerPlayer player) return player;
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
