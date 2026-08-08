package io.github.redrain0o0.globaladvancements.network;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClientboundCriterionEventPayload(Identifier trigger, Identifier value) implements CustomPacketPayload {
    public static final Identifier ID = Globaladvancements.createId("criterion_event");
    public static final Type<ClientboundCriterionEventPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundCriterionEventPayload> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            ClientboundCriterionEventPayload::trigger,
            Identifier.STREAM_CODEC,
            ClientboundCriterionEventPayload::value,
            ClientboundCriterionEventPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
