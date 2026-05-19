package io.github.redrain0o0.globaladvancements.client;

import io.github.redrain0o0.globaladvancements.client.advancements.ClientAdvancement;

import java.util.List;

public record AdvancementsFile(List<ClientAdvancement> advancements, int dataVersion) {
}
