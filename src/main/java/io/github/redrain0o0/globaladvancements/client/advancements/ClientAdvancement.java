package io.github.redrain0o0.globaladvancements.client.advancements;

import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ClientAdvancement(Identifier id, Optional<Identifier> parent, Optional<DisplayInfo> display,
                                Map<String, ClientCriterion> criteria, List<List<String>> requirements) {
    public ClientAdvancement {
        Objects.requireNonNull(id);
        Objects.requireNonNull(parent);
        Objects.requireNonNull(display);
        Objects.requireNonNull(criteria);
        Objects.requireNonNull(requirements);

        criteria = Collections.unmodifiableMap(new LinkedHashMap<>(criteria));
        requirements = requirements.stream().map(List::copyOf).toList();
    }

    public List<String> criterion() {
        return List.copyOf(criteria.keySet());
    }
}
