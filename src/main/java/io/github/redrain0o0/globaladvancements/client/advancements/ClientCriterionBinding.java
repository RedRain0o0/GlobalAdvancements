package io.github.redrain0o0.globaladvancements.client.advancements;

import java.util.Objects;

public record ClientCriterionBinding(ClientAdvancement advancement, String name, ClientCriterion criterion) {
    public ClientCriterionBinding {
        Objects.requireNonNull(advancement);
        Objects.requireNonNull(name);
        Objects.requireNonNull(criterion);
    }
}
