package io.github.redrain0o0.globaladvancements.client.advancements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.client.screen.GlobalAdvancementsScreen;
import io.github.redrain0o0.globaladvancements.criterion.CriterionEventTypes;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientCriterionManager {
    public static final Identifier OPEN_INVENTORY = Globaladvancements.createId("open_inventory");
    public static final Identifier MINECRAFT_ADVANCEMENT = Globaladvancements.createId("minecraft_advancement");
    public static final Identifier MINECART_DISTANCE = Globaladvancements.createId("minecart_distance");
    public static final Identifier MINECART_RAIL = Globaladvancements.createId("minecart_rail");

    private static final Map<Identifier, Evaluator> EVALUATORS = new ConcurrentHashMap<>();
    private static final Map<Identifier, JsonObject> MINECRAFT_ADVANCEMENTS = new LinkedHashMap<>();
    private static boolean initialized;
    private static AbstractMinecart activeMinecart;
    private static Vec3 minecartStart;

    private ClientCriterionManager() {
    }

    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        register(OPEN_INVENTORY, (conditions, event) -> true);
        register(CriterionEventTypes.CRAFT_ITEM, (conditions, event) ->
                matchesItem(conditions, "result", eventValue(event)));
        register(CriterionEventTypes.SMELT_ITEM, (conditions, event) ->
                matchesItem(conditions, "result", eventValue(event)));
        register(CriterionEventTypes.PICKUP_ITEM, (conditions, event) ->
                matchesItem(conditions, "item", eventValue(event)));
        register(CriterionEventTypes.MINE_BLOCK, ClientCriterionManager::matchesBlock);
        register(CriterionEventTypes.KILL_ENTITY, ClientCriterionManager::matchesEntity);
        register(CriterionEventTypes.FALL_FROM_VEHICLE, ClientCriterionManager::matchesEntity);
        register(CriterionEventTypes.TAME_WOLF, ClientCriterionManager::matchesTamedWolves);
        register(CriterionEventTypes.LIGHT_NETHER_PORTAL, ClientCriterionManager::matchesBlock);
        register(MINECRAFT_ADVANCEMENT, ClientCriterionManager::matchesMinecraftAdvancement);
        register(MINECART_DISTANCE, ClientCriterionManager::matchesMinecartDistance);
        register(MINECART_RAIL, ClientCriterionManager::matchesMinecartDistance);
        ClientTickEvents.END_CLIENT_TICK.register(ClientCriterionManager::tickMinecartDistance);
    }

    public static boolean register(Identifier trigger, Evaluator evaluator) {
        Objects.requireNonNull(trigger);
        Objects.requireNonNull(evaluator);
        return EVALUATORS.putIfAbsent(trigger, evaluator) == null;
    }

    public static void trigger(Identifier trigger) {
        trigger(trigger, new JsonObject());
    }

    public static void trigger(Identifier trigger, Identifier value) {
        JsonObject event = new JsonObject();
        event.addProperty("value", value.toString());
        trigger(trigger, event);
    }

    public static void trigger(Identifier trigger, JsonObject event) {
        if (CriterionEventTypes.TAME_WOLF.equals(trigger)) {
            event.addProperty("count", ClientProgressManager.recordTamedWolf());
        }
        trigger(trigger, event, true, true, true);
    }

    private static boolean trigger(Identifier trigger, JsonObject event, boolean showToast, boolean refreshScreen, boolean saveProgress) {
        Evaluator evaluator = EVALUATORS.get(trigger);
        if (evaluator == null) {
            return false;
        }

        boolean changed = false;
        for (ClientCriterionBinding binding : ClientAdvancementManager.bindings(trigger)) {
            try {
                if (!evaluator.matches(binding.criterion().conditions(), event.deepCopy())) {
                    continue;
                }

                boolean alreadyComplete = ClientProgressManager.completedCriteria(binding.advancement().id()).contains(binding.name());
                if (ClientProgressManager.completeCriterion(binding.advancement(), binding.name(), false)
                        && showToast
                        && (!MINECRAFT_ADVANCEMENT.equals(trigger) || !ClientAdvancementManager.isVanilla(binding.advancement().id()))) {
                    showToast(binding.advancement());
                }
                changed |= !alreadyComplete;
            } catch (RuntimeException exception) {
                Globaladvancements.LOGGER.warn("Failed to evaluate client criterion '{}' for '{}'", binding.name(), binding.advancement().id(), exception);
            }
        }
        if (changed && saveProgress) {
            ClientProgressManager.save();
        }
        if (changed && refreshScreen) {
            GlobalAdvancementsScreen.refreshIfOpen();
        }
        return changed;
    }

    public static void updateMinecraftAdvancements(ClientboundUpdateAdvancementsPacket packet) {
        boolean ignoreExisting = ClientProgressManager.shouldIgnoreExistingVanillaProgress();
        if (packet.shouldReset()) {
            MINECRAFT_ADVANCEMENTS.clear();
        }
        packet.getRemoved().forEach(MINECRAFT_ADVANCEMENTS::remove);
        boolean progressChanged = false;
        for (Map.Entry<Identifier, AdvancementProgress> entry : packet.getProgress().entrySet()) {
            JsonObject event = minecraftAdvancementEvent(entry.getKey(), entry.getValue());
            JsonObject previous = MINECRAFT_ADVANCEMENTS.put(entry.getKey(), event);
            if (ignoreExisting && packet.shouldReset()) {
                continue;
            }
            progressChanged |= trigger(
                    MINECRAFT_ADVANCEMENT,
                    ignoreExisting ? minecraftAdvancementChanges(event, previous) : event,
                    !packet.shouldReset() && packet.shouldShowAdvancements(),
                    false,
                    false
            );
        }
        if (progressChanged) {
            ClientProgressManager.save();
        }
        if (progressChanged) {
            GlobalAdvancementsScreen.refreshIfOpen();
        }
    }

    public static void replayMinecraftAdvancements() {
        if (ClientProgressManager.shouldIgnoreExistingVanillaProgress()) {
            return;
        }
        boolean changed = false;
        for (JsonObject event : MINECRAFT_ADVANCEMENTS.values()) {
            changed |= trigger(MINECRAFT_ADVANCEMENT, event, false, false, false);
        }
        if (changed) {
            ClientProgressManager.save();
        }
    }

    public static void clearMinecraftAdvancements() {
        MINECRAFT_ADVANCEMENTS.clear();
    }

    private static JsonObject minecraftAdvancementEvent(Identifier advancement, AdvancementProgress progress) {
        JsonObject event = new JsonObject();
        event.addProperty("advancement", advancement.toString());
        event.addProperty("complete", progress.isDone());
        JsonArray criteria = new JsonArray();
        for (String criterion : progress.getCompletedCriteria()) {
            criteria.add(criterion);
        }
        event.add("criteria", criteria);
        return event;
    }

    private static JsonObject minecraftAdvancementChanges(JsonObject event, JsonObject previous) {
        if (previous == null) {
            return event;
        }

        JsonObject changes = new JsonObject();
        changes.addProperty("advancement", event.get("advancement").getAsString());
        changes.addProperty("complete", event.get("complete").getAsBoolean() && !previous.get("complete").getAsBoolean());
        HashSet<String> previousCriteria = new HashSet<>();
        for (JsonElement criterion : previous.getAsJsonArray("criteria")) {
            previousCriteria.add(criterion.getAsString());
        }
        JsonArray criteria = new JsonArray();
        for (JsonElement criterion : event.getAsJsonArray("criteria")) {
            if (!previousCriteria.contains(criterion.getAsString())) {
                criteria.add(criterion.deepCopy());
            }
        }
        changes.add("criteria", criteria);
        return changes;
    }

    private static boolean matchesItem(JsonObject conditions, String field, Identifier value) {
        if (value == null) {
            return false;
        }

        String actualField = conditions.has(field) ? field : "item";
        return matchesRegistry(conditions.get(actualField), value, BuiltInRegistries.ITEM, Registries.ITEM);
    }

    private static boolean matchesBlock(JsonObject conditions, JsonObject event) {
        Identifier value = eventValue(event);
        if (value == null) {
            return false;
        }

        JsonElement condition = conditions.has("blocks") ? conditions.get("blocks") : conditions.get("block");
        return matchesRegistry(condition, value, BuiltInRegistries.BLOCK, Registries.BLOCK);
    }

    private static boolean matchesEntity(JsonObject conditions, JsonObject event) {
        Identifier value = eventValue(event);
        if (value == null || !matchesRegistry(conditions.get("entity"), value, BuiltInRegistries.ENTITY_TYPE, Registries.ENTITY_TYPE)) {
            return false;
        }

        if (!conditions.has("category")) {
            return true;
        }

        return BuiltInRegistries.ENTITY_TYPE.getOptional(value)
                .map(type -> matchesText(conditions.get("category"), type.getCategory().getSerializedName()))
                .orElse(false);
    }

    private static boolean matchesTamedWolves(JsonObject conditions, JsonObject event) {
        if (!matchesEntity(conditions, event) || !isNumber(event.get("count"))) {
            return false;
        }

        int required = isNumber(conditions.get("count")) ? conditions.get("count").getAsInt() : 5;
        return event.get("count").getAsInt() >= required;
    }

    private static boolean matchesMinecraftAdvancement(JsonObject conditions, JsonObject event) {
        if (!isString(conditions.get("advancement")) || !isString(event.get("advancement"))) {
            return false;
        }

        Identifier expected = Identifier.tryParse(conditions.get("advancement").getAsString());
        Identifier actual = Identifier.tryParse(event.get("advancement").getAsString());
        if (expected == null || !expected.equals(actual)) {
            return false;
        }

        if (!conditions.has("criterion")) {
            return event.has("complete") && isBoolean(event.get("complete")) && event.get("complete").getAsBoolean();
        }

        if (!isString(conditions.get("criterion")) || !event.has("criteria") || !event.get("criteria").isJsonArray()) {
            return false;
        }

        String expectedCriterion = conditions.get("criterion").getAsString();
        for (JsonElement completedCriterion : event.getAsJsonArray("criteria")) {
            if (isString(completedCriterion) && expectedCriterion.equals(completedCriterion.getAsString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesMinecartDistance(JsonObject conditions, JsonObject event) {
        if (!isNumber(event.get("distance"))) {
            return false;
        }

        double distance = event.get("distance").getAsDouble();
        if (!conditions.has("distance")) {
            return distance > 0.0;
        }

        JsonElement requirement = conditions.get("distance");
        if (isNumber(requirement)) {
            return distance >= requirement.getAsDouble();
        }

        if (!requirement.isJsonObject()) {
            return false;
        }

        JsonObject range = requirement.getAsJsonObject();
        if (range.has("min") && (!isNumber(range.get("min")) || distance < range.get("min").getAsDouble())) {
            return false;
        }
        return !range.has("max") || isNumber(range.get("max")) && distance <= range.get("max").getAsDouble();
    }

    private static <T> boolean matchesRegistry(JsonElement condition, Identifier value, Registry<T> registry,
                                                ResourceKey<? extends Registry<T>> registryKey) {
        if (condition == null) {
            return true;
        }

        if (condition.isJsonArray()) {
            for (JsonElement entry : condition.getAsJsonArray()) {
                if (matchesRegistry(entry, value, registry, registryKey)) {
                    return true;
                }
            }
            return false;
        }

        if (!isString(condition)) {
            return false;
        }

        String expected = condition.getAsString();
        if (!expected.startsWith("#")) {
            Identifier expectedId = Identifier.tryParse(expected);
            return expectedId != null && expectedId.equals(value);
        }

        Identifier tagId = Identifier.tryParse(expected.substring(1));
        if (tagId == null) {
            return false;
        }

        TagKey<T> tag = TagKey.create(registryKey, tagId);
        return registry.get(value).map(holder -> holder.is(tag)).orElse(false);
    }

    private static boolean matchesText(JsonElement condition, String value) {
        if (condition == null) {
            return true;
        }
        if (condition.isJsonArray()) {
            for (JsonElement entry : condition.getAsJsonArray()) {
                if (matchesText(entry, value)) {
                    return true;
                }
            }
            return false;
        }
        if (!isString(condition)) {
            return false;
        }
        return value.equals(condition.getAsString());
    }

    private static Identifier eventValue(JsonObject event) {
        if (!isString(event.get("value"))) {
            return null;
        }
        return Identifier.tryParse(event.get("value").getAsString());
    }

    private static boolean isString(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
    }

    private static boolean isNumber(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
    }

    private static boolean isBoolean(JsonElement element) {
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean();
    }

    private static void tickMinecartDistance(Minecraft minecraft) {
        if (minecraft.player == null || !(minecraft.player.getVehicle() instanceof AbstractMinecart minecart)) {
            activeMinecart = null;
            minecartStart = null;
            return;
        }

        if (minecart != activeMinecart || minecartStart == null) {
            activeMinecart = minecart;
            minecartStart = minecart.position();
            return;
        }

        Vec3 position = minecart.position();
        double x = position.x() - minecartStart.x();
        double z = position.z() - minecartStart.z();
        JsonObject event = new JsonObject();
        event.addProperty("distance", Math.sqrt(x * x + z * z));
        trigger(MINECART_DISTANCE, event);
        trigger(MINECART_RAIL, event);
    }

    private static void showToast(ClientAdvancement advancement) {
        advancement.display().filter(display -> display.shouldShowToast()).ifPresent(display ->
                Minecraft.getInstance().getToastManager().addToast(
                        new AdvancementToast(ClientAdvancementView.createHolder(advancement, true))
                )
        );
    }

    @FunctionalInterface
    public interface Evaluator {
        boolean matches(JsonObject conditions, JsonObject event);
    }
}
