package io.github.redrain0o0.globaladvancements.client.advancements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.client.screen.GlobalAdvancementsScreen;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStackTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ClientAdvancementManager implements SimpleSynchronousResourceReloadListener {
    public static final ClientAdvancementManager INSTANCE = new ClientAdvancementManager();

    private static final String ADVANCEMENTS_FOLDER = "advancements";
    private static final String VANILLA_ADVANCEMENTS_FOLDER = "advancement";
    private static final RegistryOps<JsonElement> DISPLAY_OPS = RegistryOps.create(
            JsonOps.INSTANCE,
            HolderLookup.Provider.create(BuiltInRegistries.REGISTRY.stream().map(registry -> registry))
    );
    private static Map<Identifier, ClientAdvancement> resourceAdvancements = Map.of();
    private static Map<Identifier, ClientAdvancement> vanillaAdvancements = Map.of();
    private static volatile Snapshot snapshot = Snapshot.EMPTY;

    private ClientAdvancementManager() {}

    @Override
    public Identifier getFabricId() {
        return Globaladvancements.createId("client_advancements");
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        Map<Identifier, ClientAdvancement> parsed = new LinkedHashMap<>();
        Map<Identifier, Resource> resources = resourceManager.listResources(
                ADVANCEMENTS_FOLDER,
                id -> id.getPath().endsWith(".json")
        );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier advancementId = getAdvancementId(entry.getKey());
            loadAdvancement(advancementId, entry.getValue()).ifPresent(advancement ->
                    parsed.put(advancement.id(), advancement)
            );
        }

        boolean simplifyIcons = Minecraft.getInstance().getConnection() == null;
        Map<Identifier, ClientAdvancement> vanilla = readVanillaAdvancements(getDisplayOps(), simplifyIcons)
                .orElse(vanillaAdvancements);
        setAdvancements(parsed, vanilla);
        ClientCriterionManager.replayMinecraftAdvancements();
        GlobalAdvancementsScreen.refreshIfOpen();

        Globaladvancements.LOGGER.info("Loaded {} vanilla advancements, {} resource pack advancements and {} active client advancements", vanillaAdvancements.size(), resourceAdvancements.size(), snapshot.advancements().size());
    }

    public static boolean loadVanillaAdvancements(HolderLookup.Provider registries) {
        Optional<Map<Identifier, ClientAdvancement>> parsed = readVanillaAdvancements(
                registries.createSerializationContext(JsonOps.INSTANCE),
                false
        );
        if (parsed.isEmpty()) {
            return false;
        }

        setVanillaAdvancements(parsed.get());
        GlobalAdvancementsScreen.refreshIfOpen();
        Globaladvancements.LOGGER.info("Loaded {} vanilla advancements", vanillaAdvancements.size());
        return true;
    }

    private static Optional<Map<Identifier, ClientAdvancement>> readVanillaAdvancements(
            RegistryOps<JsonElement> displayOps,
            boolean simplifyIcons
    ) {
        Map<Identifier, ClientAdvancement> parsed = new LinkedHashMap<>();
        try (CloseableResourceManager resources = new MultiPackResourceManager(
                PackType.SERVER_DATA,
                List.of(ServerPacksSource.createVanillaPackSource())
        )) {
            Map<Identifier, Resource> advancements = resources.listResources(
                    VANILLA_ADVANCEMENTS_FOLDER,
                    id -> id.getPath().endsWith(".json")
                            && !id.getPath().startsWith(VANILLA_ADVANCEMENTS_FOLDER + "/recipes/")
            );
            for (Map.Entry<Identifier, Resource> entry : advancements.entrySet()) {
                Identifier advancementId = getAdvancementId(entry.getKey(), VANILLA_ADVANCEMENTS_FOLDER);
                loadVanillaAdvancement(advancementId, entry.getValue(), displayOps, simplifyIcons).ifPresent(advancement ->
                        parsed.put(advancement.id(), advancement)
                );
            }
        } catch (RuntimeException exception) {
            Globaladvancements.LOGGER.warn("Failed to load vanilla advancements", exception);
            return Optional.empty();
        }
        return Optional.of(parsed);
    }

    public static Collection<ClientAdvancement> all() {
        return snapshot.advancements().values();
    }

    public static List<ClientCriterionBinding> bindings(Identifier trigger) {
        return snapshot.bindings().getOrDefault(trigger, List.of());
    }

    public static List<ClientAdvancement> roots() {
        return snapshot.roots();
    }

    public static List<ClientAdvancement> childrenOf(Identifier parentId) {
        return snapshot.children().getOrDefault(parentId, List.of());
    }

    public static int size() {
        return snapshot.advancements().size();
    }

    public static Optional<ClientAdvancement> get(Identifier id) {
        return Optional.ofNullable(snapshot.advancements().get(id));
    }

    public static synchronized boolean isVanilla(Identifier id) {
        return vanillaAdvancements.containsKey(id) && !resourceAdvancements.containsKey(id);
    }

    private static synchronized void setAdvancements(Map<Identifier, ClientAdvancement> resources,
                                                     Map<Identifier, ClientAdvancement> vanilla) {
        resourceAdvancements = Collections.unmodifiableMap(new LinkedHashMap<>(resources));
        vanillaAdvancements = Collections.unmodifiableMap(new LinkedHashMap<>(vanilla));
        rebuildSnapshot();
    }

    private static synchronized void setVanillaAdvancements(Map<Identifier, ClientAdvancement> advancements) {
        vanillaAdvancements = Collections.unmodifiableMap(new LinkedHashMap<>(advancements));
        rebuildSnapshot();
    }

    private static void rebuildSnapshot() {
        Map<Identifier, ClientAdvancement> merged = new LinkedHashMap<>(vanillaAdvancements);
        merged.putAll(resourceAdvancements);
        snapshot = Snapshot.create(validateParents(merged));
        ClientProgressManager.preserveCompletions(snapshot.advancements().values());
    }

    private static Optional<ClientAdvancement> loadVanillaAdvancement(Identifier advancementId,
                                                                      Resource resource,
                                                                      RegistryOps<JsonElement> displayOps,
                                                                      boolean simplifyIcons) {
        try (BufferedReader reader = resource.openAsReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (!json.has("display")) {
                return Optional.empty();
            }

            Optional<DisplayInfo> display = getDisplay(advancementId, json, displayOps, !simplifyIcons);
            if (display.isEmpty() && simplifyIcons) {
                JsonObject simplified = json.deepCopy();
                simplified.getAsJsonObject("display").getAsJsonObject("icon").remove("components");
                display = getDisplay(advancementId, simplified, displayOps, true);
            }
            if (display.isEmpty()) {
                throw new IllegalArgumentException("Invalid display");
            }

            Map<String, ClientCriterion> criteria = getVanillaCriteria(advancementId, json);
            List<List<String>> requirements = getRequirements(json, criteria);
            JsonObject completeConditions = new JsonObject();
            completeConditions.addProperty("advancement", advancementId.toString());
            List<String> completionCriteria = new ArrayList<>();
            criteria.put("complete", new ClientCriterion(ClientCriterionManager.MINECRAFT_ADVANCEMENT, completeConditions));
            completionCriteria.add("complete");
            if ("minecraft:story/mine_stone".equals(advancementId.toString())) {
                criteria.put("minecraft:mine_stone", new ClientCriterion(ClientCriterionManager.MINECRAFT_ADVANCEMENT, completeConditions));
                completionCriteria.add("minecraft:mine_stone");
            }
            return Optional.of(new ClientAdvancement(
                    advancementId,
                    getParent(json),
                    display,
                    criteria,
                    requirements.stream().map(requirement -> {
                        List<String> withComplete = new ArrayList<>(requirement);
                        withComplete.addAll(completionCriteria);
                        return List.copyOf(withComplete);
                    }).toList()
            ));
        } catch (IOException | RuntimeException exception) {
            Globaladvancements.LOGGER.warn("Failed to load vanilla advancement '{}'", advancementId, exception);
            return Optional.empty();
        }
    }

    private static Map<String, ClientCriterion> getVanillaCriteria(Identifier advancementId, JsonObject json) {
        Map<String, ClientCriterion> criteria = new LinkedHashMap<>();
        JsonObject definitions = json.getAsJsonObject("criteria");
        for (String name : definitions.keySet()) {
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Criterion names cannot be empty");
            }

            JsonObject conditions = new JsonObject();
            conditions.addProperty("advancement", advancementId.toString());
            conditions.addProperty("criterion", name);
            criteria.put(name, new ClientCriterion(ClientCriterionManager.MINECRAFT_ADVANCEMENT, conditions));
        }
        return criteria;
    }

    private static Optional<ClientAdvancement> loadAdvancement(Identifier advancementId, Resource resource) {
        try (BufferedReader reader = resource.openAsReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            Optional<Identifier> parent = getParent(json);
            Optional<DisplayInfo> display = getDisplay(advancementId, json);
            if (json.has("display") && display.isEmpty()) {
                throw new IllegalArgumentException("Invalid display");
            }
            Map<String, ClientCriterion> criteria = getCriteria(json);
            List<List<String>> requirements = getRequirements(json, criteria);

            return Optional.of(new ClientAdvancement(
                    advancementId,
                    parent,
                    display,
                    criteria,
                    requirements
            ));
        } catch (IOException | RuntimeException exception) {
            Globaladvancements.LOGGER.warn("Failed to load client advancement '{}'", advancementId, exception);
            return Optional.empty();
        }
    }

    private static Identifier getAdvancementId(Identifier fileId) {
        return getAdvancementId(fileId, ADVANCEMENTS_FOLDER);
    }

    private static Identifier getAdvancementId(Identifier fileId, String folder) {
        String path = fileId.getPath();
        path = path.substring(folder.length() + 1);
        path = path.substring(0, path.length() - ".json".length());

        return Identifier.fromNamespaceAndPath(fileId.getNamespace(), path);
    }

    private static Optional<Identifier> getParent(JsonObject json) {
        if (!json.has("parent")) {
            return Optional.empty();
        }

        return Optional.of(Identifier.parse(json.get("parent").getAsString()));
    }

    private static Optional<DisplayInfo> getDisplay(Identifier advancementId, JsonObject json) {
        return getDisplay(advancementId, json, getDisplayOps(), true);
    }

    private static RegistryOps<JsonElement> getDisplayOps() {
        if (Minecraft.getInstance().getConnection() != null) {
            return Minecraft.getInstance().getConnection().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        }
        return DISPLAY_OPS;
    }

    private static Optional<DisplayInfo> getDisplay(Identifier advancementId, JsonObject json,
                                                    RegistryOps<JsonElement> displayOps,
                                                    boolean logErrors) {
        if (!json.has("display")) {
            return Optional.empty();
        }

        JsonObject display = json.getAsJsonObject("display").deepCopy();
        if (json.has("background") && !display.has("background")) {
            display.add("background", json.get("background"));
        }

        if (logErrors) {
            return DisplayInfo.CODEC.parse(displayOps, display)
                    .resultOrPartial(error -> Globaladvancements.LOGGER.warn("Failed to parse display for '{}': {}", advancementId, error))
                    .map(ClientAdvancementManager::bindIcon);
        }
        return DisplayInfo.CODEC.parse(displayOps, display).result().map(ClientAdvancementManager::bindIcon);
    }

    private static DisplayInfo bindIcon(DisplayInfo display) {
        ItemStackTemplate icon = display.getIcon();
        Identifier itemId = icon.item().unwrapKey().orElseThrow().identifier();
        DataComponentPatch.Builder components = DataComponentPatch.builder();
        icon.components().entrySet().forEach(entry -> copyComponent(components, entry));
        components.set(DataComponents.ITEM_MODEL, itemId);

        ItemStackTemplate boundIcon = new ItemStackTemplate(
                Holder.direct(icon.item().value(), DataComponentMap.EMPTY),
                icon.count(),
                components.build()
        );
        return new DisplayInfo(
                boundIcon,
                display.getTitle(),
                display.getDescription(),
                display.getBackground(),
                display.getType(),
                display.shouldShowToast(),
                display.shouldAnnounceChat(),
                display.isHidden()
        );
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void copyComponent(DataComponentPatch.Builder components, Map.Entry<DataComponentType<?>, Optional<?>> entry) {
        DataComponentType type = entry.getKey();
        if (entry.getValue().isPresent()) {
            components.set(type, entry.getValue().get());
        } else {
            components.remove(type);
        }
    }

    private static Map<String, ClientCriterion> getCriteria(JsonObject json) {
        if (!json.has("criteria")) {
            return Map.of();
        }

        Map<String, ClientCriterion> criteria = new LinkedHashMap<>();
        JsonObject definitions = json.getAsJsonObject("criteria");
        for (Map.Entry<String, JsonElement> entry : definitions.entrySet()) {
            String name = entry.getKey();
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Criterion names cannot be empty");
            }

            JsonObject definition = entry.getValue().getAsJsonObject();
            if (!definition.has("trigger")) {
                throw new IllegalArgumentException("Criterion '" + name + "' is missing a trigger");
            }

            Identifier trigger = Identifier.parse(definition.get("trigger").getAsString());
            JsonObject conditions = definition.has("conditions")
                    ? definition.getAsJsonObject("conditions")
                    : new JsonObject();
            criteria.put(name, new ClientCriterion(trigger, conditions));
        }
        return criteria;
    }

    private static List<List<String>> getRequirements(JsonObject json, Map<String, ClientCriterion> criteria) {
        if (!json.has("requirements")) {
            return criteria.keySet().stream().map(List::of).toList();
        }

        List<List<String>> requirements = new ArrayList<>();
        Set<String> referenced = new LinkedHashSet<>();
        JsonArray requirementArray = json.getAsJsonArray("requirements");
        for (JsonElement requirementElement : requirementArray) {
            JsonArray names = requirementElement.getAsJsonArray();
            if (names.isEmpty()) {
                throw new IllegalArgumentException("Requirement groups cannot be empty");
            }

            List<String> requirement = new ArrayList<>();
            for (JsonElement criterionElement : names) {
                String name = criterionElement.getAsString();
                if (!criteria.containsKey(name)) {
                    throw new IllegalArgumentException("Requirement references unknown criterion '" + name + "'");
                }
                requirement.add(name);
                referenced.add(name);
            }
            requirements.add(List.copyOf(requirement));
        }

        if (!referenced.containsAll(criteria.keySet())) {
            Set<String> missing = new LinkedHashSet<>(criteria.keySet());
            missing.removeAll(referenced);
            throw new IllegalArgumentException("Requirements omit criteria " + missing);
        }

        return List.copyOf(requirements);
    }

    private static Map<Identifier, ClientAdvancement> validateParents(Map<Identifier, ClientAdvancement> parsed) {
        Map<Identifier, ClientAdvancement> validated = new LinkedHashMap<>(parsed);
        removeMissingParents(validated);

        Set<Identifier> cycles = findCycles(validated);
        for (Identifier id : cycles) {
            validated.remove(id);
            Globaladvancements.LOGGER.warn("Ignoring client advancement '{}' because its parent chain contains a cycle", id);
        }

        removeMissingParents(validated);
        return validated;
    }

    private static void removeMissingParents(Map<Identifier, ClientAdvancement> advancements) {
        boolean removed;
        do {
            removed = false;
            Iterator<Map.Entry<Identifier, ClientAdvancement>> iterator = advancements.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Identifier, ClientAdvancement> entry = iterator.next();
                Optional<Identifier> parent = entry.getValue().parent();
                if (parent.isPresent() && !advancements.containsKey(parent.get())) {
                    iterator.remove();
                    removed = true;
                    Globaladvancements.LOGGER.warn("Ignoring client advancement '{}' because parent '{}' is unavailable", entry.getKey(), parent.get());
                }
            }
        } while (removed);
    }

    private static Set<Identifier> findCycles(Map<Identifier, ClientAdvancement> advancements) {
        Set<Identifier> cycles = new LinkedHashSet<>();
        Set<Identifier> resolved = new HashSet<>();

        for (Identifier start : advancements.keySet()) {
            if (resolved.contains(start)) {
                continue;
            }

            List<Identifier> path = new ArrayList<>();
            Map<Identifier, Integer> positions = new LinkedHashMap<>();
            Identifier current = start;
            while (current != null && advancements.containsKey(current) && !resolved.contains(current)) {
                Integer position = positions.putIfAbsent(current, path.size());
                if (position != null) {
                    cycles.addAll(path.subList(position, path.size()));
                    break;
                }

                path.add(current);
                current = advancements.get(current).parent().orElse(null);
            }
            resolved.addAll(path);
        }

        return cycles;
    }

    private record Snapshot(Map<Identifier, ClientAdvancement> advancements,
                            List<ClientAdvancement> roots,
                            Map<Identifier, List<ClientAdvancement>> children,
                            Map<Identifier, List<ClientCriterionBinding>> bindings) {
        private static final Snapshot EMPTY = new Snapshot(Map.of(), List.of(), Map.of(), Map.of());

        private static Snapshot create(Map<Identifier, ClientAdvancement> advancements) {
            Map<Identifier, ClientAdvancement> advancementSnapshot = Collections.unmodifiableMap(new LinkedHashMap<>(advancements));
            List<ClientAdvancement> roots = new ArrayList<>();
            Map<Identifier, List<ClientAdvancement>> children = new LinkedHashMap<>();
            Map<Identifier, List<ClientCriterionBinding>> bindings = new LinkedHashMap<>();

            for (ClientAdvancement advancement : advancementSnapshot.values()) {
                if (advancement.parent().isEmpty()) {
                    roots.add(advancement);
                } else {
                    children.computeIfAbsent(advancement.parent().get(), id -> new ArrayList<>()).add(advancement);
                }

                for (Map.Entry<String, ClientCriterion> criterion : advancement.criteria().entrySet()) {
                    ClientCriterionBinding binding = new ClientCriterionBinding(advancement, criterion.getKey(), criterion.getValue());
                    bindings.computeIfAbsent(criterion.getValue().trigger(), id -> new ArrayList<>()).add(binding);
                }
            }

            return new Snapshot(
                    advancementSnapshot,
                    List.copyOf(roots),
                    immutableLists(children),
                    immutableLists(bindings)
            );
        }

        private static <K, V> Map<K, List<V>> immutableLists(Map<K, List<V>> values) {
            Map<K, List<V>> copy = new LinkedHashMap<>();
            values.forEach((key, value) -> copy.put(key, List.copyOf(value)));
            return Collections.unmodifiableMap(copy);
        }
    }
}
