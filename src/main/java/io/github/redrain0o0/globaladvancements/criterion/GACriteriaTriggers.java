package io.github.redrain0o0.globaladvancements.criterion;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class GACriteriaTriggers {
    public static final MineBlockCriterion MINE_BLOCK = register("mine_block", new MineBlockCriterion());
    public static final MinecartRailCriterion MINECART_RAIL = register("minecart_rail", new MinecartRailCriterion());
    public static final OpenInventoryCriterion OPEN_INVENTORY = register("open_inventory", new OpenInventoryCriterion());

    public static void initialize() {
        MINE_BLOCK.initialize();
    }

    public static <T extends CriterionTrigger<?>> T register(final String name, final T criterion) {
        return Registry.register(BuiltInRegistries.TRIGGER_TYPES, Globaladvancements.createId(name), criterion);
    }
}
