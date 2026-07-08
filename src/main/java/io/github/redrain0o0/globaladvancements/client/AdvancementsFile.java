package io.github.redrain0o0.globaladvancements.client;

import com.google.gson.annotations.SerializedName;
import io.github.redrain0o0.globaladvancements.client.advancements.ClientAdvancement;

import java.util.List;

public record AdvancementsFile(List<ClientAdvancement> advancements, @SerializedName("completed_criteria") List<String> completedCriteria, int dataVersion) {
    public AdvancementsFile(List<ClientAdvancement> advancements, int dataVersion) {
        this(advancements, List.of(), dataVersion);
    }
}
