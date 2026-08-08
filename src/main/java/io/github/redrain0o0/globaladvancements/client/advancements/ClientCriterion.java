package io.github.redrain0o0.globaladvancements.client.advancements;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public record ClientCriterion(Identifier trigger, JsonObject conditions) {
    public ClientCriterion {
        Objects.requireNonNull(trigger);
        Objects.requireNonNull(conditions);
        conditions = conditions.deepCopy();
    }

    @Override
    public JsonObject conditions() {
        return conditions.deepCopy();
    }
}
