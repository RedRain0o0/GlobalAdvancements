package io.github.redrain0o0.globaladvancements.client.advancements;

import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public record ClientAdvancement(Optional<Identifier> parent, Optional<DisplayInfo> display/*, Map<String, Criterion<?>> criteria, AdvancementRequirements requirements*/) {

}
