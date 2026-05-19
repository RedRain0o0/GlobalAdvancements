package io.github.redrain0o0.globaladvancements.network;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundModCheckPayload(boolean unused) implements CustomPacketPayload {
    public static final Identifier MOD_CHECK_PAYLOAD_ID = Globaladvancements.createId("mod_check_clientbound");
    public static final CustomPacketPayload.Type<ClientboundModCheckPayload> TYPE = new CustomPacketPayload.Type<>(MOD_CHECK_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundModCheckPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ClientboundModCheckPayload::unused,
            ClientboundModCheckPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
