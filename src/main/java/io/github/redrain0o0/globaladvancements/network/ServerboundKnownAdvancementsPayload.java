package io.github.redrain0o0.globaladvancements.network;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record ServerboundKnownAdvancementsPayload(List<Identifier> advancementIds) implements CustomPacketPayload {
    public static final Identifier KNOWN_ADVANCEMENTS_PAYLOAD_ID = Globaladvancements.createId("known_advancements_serverbound");
    public static final Type<ServerboundKnownAdvancementsPayload> TYPE = new Type<>(KNOWN_ADVANCEMENTS_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundKnownAdvancementsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, Identifier.STREAM_CODEC),
            ServerboundKnownAdvancementsPayload::advancementIds,
            ServerboundKnownAdvancementsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
