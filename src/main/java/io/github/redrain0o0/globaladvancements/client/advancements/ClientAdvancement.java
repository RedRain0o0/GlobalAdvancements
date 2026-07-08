package io.github.redrain0o0.globaladvancements.client.advancements;

import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

public record ClientAdvancement(Identifier id, Optional<Identifier> parent, Optional<DisplayInfo> display, List<String> criterion, List<List<String>> requirements) {

}
