package io.github.redrain0o0.globaladvancements.criterion;

import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.resources.Identifier;

public final class CriterionEventTypes {
    public static final Identifier CRAFT_ITEM = Globaladvancements.createId("craft_item");
    public static final Identifier SMELT_ITEM = Globaladvancements.createId("smelt_item");
    public static final Identifier PICKUP_ITEM = Globaladvancements.createId("pickup_item");
    public static final Identifier MINE_BLOCK = Globaladvancements.createId("mine_block");
    public static final Identifier KILL_ENTITY = Globaladvancements.createId("kill_entity");
    public static final Identifier FALL_FROM_VEHICLE = Globaladvancements.createId("fall_from_vehicle");
    public static final Identifier TAME_WOLF = Globaladvancements.createId("tame_wolf");
    public static final Identifier LIGHT_NETHER_PORTAL = Globaladvancements.createId("light_nether_portal");
    public static final Identifier SNIPER_DUEL = Globaladvancements.createId("sniper_duel");
    public static final Identifier DIAMONDS_TO_YOU = Globaladvancements.createId("diamonds_to_you");

    private CriterionEventTypes() {
    }
}
