package io.github.redrain0o0.globaladvancements.network;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundAdvancementHolderIdPayload(Identifier advancementHolderId, String criterion) implements CustomPacketPayload {
    public static final Identifier ADVANCEMENT_HOLDER_ID_PAYLOAD_ID = Globaladvancements.createId("advancement_holder_id_clientbound");
    public static final Type<ClientboundAdvancementHolderIdPayload> TYPE = new Type<>(ADVANCEMENT_HOLDER_ID_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAdvancementHolderIdPayload> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            ClientboundAdvancementHolderIdPayload::advancementHolderId,
            ByteBufCodecs.STRING_UTF8,
            ClientboundAdvancementHolderIdPayload::criterion,
            ClientboundAdvancementHolderIdPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
