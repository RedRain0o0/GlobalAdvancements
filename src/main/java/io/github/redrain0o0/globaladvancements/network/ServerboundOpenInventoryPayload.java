package io.github.redrain0o0.globaladvancements.network;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundOpenInventoryPayload(boolean unused) implements CustomPacketPayload {
    public static final Identifier OPEN_INVENTORY_PAYLOAD_ID = Globaladvancements.createId("open_inventory_serverbound");
    public static final Type<ServerboundOpenInventoryPayload> TYPE = new Type<>(OPEN_INVENTORY_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpenInventoryPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            ServerboundOpenInventoryPayload::unused,
            ServerboundOpenInventoryPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
