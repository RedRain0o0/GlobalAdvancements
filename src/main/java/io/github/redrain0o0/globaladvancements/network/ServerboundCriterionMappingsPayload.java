package io.github.redrain0o0.globaladvancements.network;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record ServerboundCriterionMappingsPayload(List<CriterionMapping> criteria) implements CustomPacketPayload {
    public static final Identifier CRITERION_MAPPINGS_PAYLOAD_ID = Globaladvancements.createId("criterion_mappings_serverbound");
    public static final Type<ServerboundCriterionMappingsPayload> TYPE = new Type<>(CRITERION_MAPPINGS_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundCriterionMappingsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, CriterionMapping.STREAM_CODEC),
            ServerboundCriterionMappingsPayload::criteria,
            ServerboundCriterionMappingsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record CriterionMapping(Identifier serverAdvancementId, String serverCriterion, Identifier clientAdvancementId, String clientCriterion) {
        public static final StreamCodec<RegistryFriendlyByteBuf, CriterionMapping> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC,
                CriterionMapping::serverAdvancementId,
                ByteBufCodecs.STRING_UTF8,
                CriterionMapping::serverCriterion,
                Identifier.STREAM_CODEC,
                CriterionMapping::clientAdvancementId,
                ByteBufCodecs.STRING_UTF8,
                CriterionMapping::clientCriterion,
                CriterionMapping::new);

        public boolean matches(Identifier advancementId, String criterion) {
            return serverAdvancementId.equals(advancementId) && serverCriterion.equals(criterion);
        }
    }
}
