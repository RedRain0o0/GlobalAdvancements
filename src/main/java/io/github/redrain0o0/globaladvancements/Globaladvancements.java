package io.github.redrain0o0.globaladvancements;

import io.github.redrain0o0.globaladvancements.network.ClientboundModCheckPayload;
import io.github.redrain0o0.globaladvancements.network.ServerboundModCheckPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Globaladvancements implements ModInitializer {
    public static final String MOD_ID = "globaladvancements";
    public static final String MOD_NAME = "Global Advancements";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final AttachmentType<Boolean> HAS_MOD = AttachmentRegistry.create(
            createId("has_mod")
    );

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(ClientboundModCheckPayload.TYPE, ClientboundModCheckPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ServerboundModCheckPayload.TYPE, ServerboundModCheckPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ServerboundModCheckPayload.TYPE, (payload, context) -> LOGGER.info("test"));

        ServerPlayerEvents.JOIN.register((player) -> {
            boolean clientHasMod = ServerPlayNetworking.canSend(player, ClientboundModCheckPayload.TYPE);
            player.setAttached(HAS_MOD, clientHasMod);
            LOGGER.info(clientHasMod ? "Client has "+MOD_NAME+" installed" : "Client doesn't have "+MOD_NAME+" installed");
        });
    }

    public static Identifier createId(String id) {
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }
}
