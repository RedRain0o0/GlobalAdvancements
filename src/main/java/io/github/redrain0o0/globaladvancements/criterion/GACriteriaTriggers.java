package io.github.redrain0o0.globaladvancements.criterion;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class GACriteriaTriggers {
    public static final CraftItemCriterion CRAFT_ITEM = register("craft_item", new CraftItemCriterion());
    public static final KillEntityCriterion KILL_ENTITY = register("kill_entity", new KillEntityCriterion());
    public static final MineBlockCriterion MINE_BLOCK = register("mine_block", new MineBlockCriterion());
    public static final MinecartRailCriterion MINECART_RAIL = register("minecart_rail", new MinecartRailCriterion());
    public static final OpenInventoryCriterion OPEN_INVENTORY = register("open_inventory", new OpenInventoryCriterion());
    public static final PickupItemCriterion PICKUP_ITEM = register("pickup_item", new PickupItemCriterion());
    public static final SmeltItemCriterion SMELT_ITEM = register("smelt_item", new SmeltItemCriterion());

    public static void initialize() {
        KILL_ENTITY.initialize();
        MINE_BLOCK.initialize();
    }

    public static <T extends CriterionTrigger<?>> T register(final String name, final T criterion) {
        return Registry.register(BuiltInRegistries.TRIGGER_TYPES, Globaladvancements.createId(name), criterion);
    }
}
