package io.github.redrain0o0.globaladvancements.network;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundVerifyAdvancementPayload(Identifier advancementHolderId, boolean hasAdvancement) implements CustomPacketPayload {
    public static final Identifier VERIFY_ADVANCEMENT_PAYLOAD_ID = Globaladvancements.createId("verify_advancement_serverbound");
    public static final CustomPacketPayload.Type<ServerboundVerifyAdvancementPayload> TYPE = new CustomPacketPayload.Type<>(VERIFY_ADVANCEMENT_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundVerifyAdvancementPayload> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            ServerboundVerifyAdvancementPayload::advancementHolderId,
            ByteBufCodecs.BOOL,
            ServerboundVerifyAdvancementPayload::hasAdvancement,
            ServerboundVerifyAdvancementPayload::new);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
