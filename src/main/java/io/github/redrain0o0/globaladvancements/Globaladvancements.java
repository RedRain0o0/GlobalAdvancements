package io.github.redrain0o0.globaladvancements;

import com.mojang.datafixers.util.Pair;
import io.github.redrain0o0.globaladvancements.advancements.GACriteriaTriggers;
import io.github.redrain0o0.globaladvancements.network.ClientboundAdvancementHolderIdPayload;
import io.github.redrain0o0.globaladvancements.network.ClientboundModCheckPayload;
import io.github.redrain0o0.globaladvancements.network.ServerboundKnownAdvancementsPayload;
import io.github.redrain0o0.globaladvancements.network.ServerboundModCheckPayload;
import io.github.redrain0o0.globaladvancements.network.ServerboundVerifyAdvancementPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Globaladvancements implements ModInitializer {
    public static final String MOD_ID = "globaladvancements";
    public static final String MOD_NAME = "Global Advancements";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final Map<UUID, Map<Identifier, List<String>>> criterion = new HashMap<>();
    public static final Map<UUID, Set<Identifier>> clientAdvancements = new HashMap<>();
    public static final List<Pair<UUID, Identifier>> queriedAdvancements = new ArrayList<>();

    public static final AttachmentType<Boolean> HAS_MOD = AttachmentRegistry.create(
            createId("has_mod")
    );

    @Override
    public void onInitialize() {
        GACriteriaTriggers.initialize();
        PayloadTypeRegistry.clientboundPlay().register(ClientboundModCheckPayload.TYPE, ClientboundModCheckPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundAdvancementHolderIdPayload.TYPE, ClientboundAdvancementHolderIdPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundKnownAdvancementsPayload.TYPE, ServerboundKnownAdvancementsPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundModCheckPayload.TYPE, ServerboundModCheckPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundVerifyAdvancementPayload.TYPE, ServerboundVerifyAdvancementPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerboundModCheckPayload.TYPE, (payload, context) -> LOGGER.info("You shouldn't see this I dont think... The ModCheck packet is not for use, only checking"));
        ServerPlayNetworking.registerGlobalReceiver(ServerboundKnownAdvancementsPayload.TYPE, (payload, context) -> {
            clientAdvancements.put(context.player().getUUID(), new HashSet<>(payload.advancementIds()));
            LOGGER.info("Client has {} client advancements loaded", payload.advancementIds().size());
        });
        ServerPlayNetworking.registerGlobalReceiver(ServerboundVerifyAdvancementPayload.TYPE, (payload, context) -> {
            Pair<UUID, Identifier> playerAdvancementPair = new Pair<>(context.player().getUUID(), payload.advancementHolderId());
            if (queriedAdvancements.contains(playerAdvancementPair)) {
                queriedAdvancements.remove(playerAdvancementPair);
                if (payload.hasAdvancement()) return;
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
            clientAdvancements.remove(player.getUUID());
            player.setAttached(HAS_MOD, clientHasMod);
            LOGGER.info(clientHasMod ? "Client has "+MOD_NAME+" installed" : "Client doesn't have "+MOD_NAME+" installed");
        });

        ServerTickEvents.END_SERVER_TICK.register((minecraftServer) -> {
            if (criterion.isEmpty()) return;
            Iterator<Map.Entry<UUID, Map<Identifier, List<String>>>> playerIterator = criterion.entrySet().iterator();
            while (playerIterator.hasNext()) {
                Map.Entry<UUID, Map<Identifier, List<String>>> playerPair = playerIterator.next();
                ServerPlayer player = minecraftServer.getPlayerList().getPlayer(playerPair.getKey());
                if (player == null || !player.getAttachedOrElse(HAS_MOD, false)) {
                    playerIterator.remove();
                    continue;
                }
                Set<Identifier> knownAdvancements = clientAdvancements.get(player.getUUID());
                if (knownAdvancements == null) continue;
                for (Map.Entry<Identifier, List<String>> advancementPair : playerPair.getValue().entrySet()) {
                    if (!knownAdvancements.contains(advancementPair.getKey())) continue;
                    for (String criterionName : advancementPair.getValue()) {
                        queriedAdvancements.add(new Pair<>(player.getUUID(), advancementPair.getKey()));
                        ServerPlayNetworking.send(player, new ClientboundAdvancementHolderIdPayload(advancementPair.getKey(), criterionName));
                    }
                }
                playerIterator.remove();
            }
        });
    }

    // TODO: Basic query for asking client about advancements unlock state

    public static Identifier createId(String id) {
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }
}
