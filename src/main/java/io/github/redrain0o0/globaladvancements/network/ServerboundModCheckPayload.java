package io.github.redrain0o0.globaladvancements.network;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundModCheckPayload(boolean unused) implements CustomPacketPayload {
    public static final Identifier MOD_CHECK_PAYLOAD_ID = Globaladvancements.createId("mod_check_serverbound");
    public static final CustomPacketPayload.Type<ServerboundModCheckPayload> TYPE = new CustomPacketPayload.Type<>(MOD_CHECK_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundModCheckPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ServerboundModCheckPayload::unused,
            ServerboundModCheckPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
