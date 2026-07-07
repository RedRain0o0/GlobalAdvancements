package io.github.redrain0o0.globaladvancements.advancements;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class GACriteriaTriggers {
    public static final MinecartRailCriterion MINECART_RAIL;

    public static void initialize() {}

    public static <T extends CriterionTrigger<?>> T register(final String name, final T criterion) {
        return Registry.register(BuiltInRegistries.TRIGGER_TYPES, name, criterion);
    }

    static {
        MINECART_RAIL = register("minecart_rail", new MinecartRailCriterion());
    }
}
