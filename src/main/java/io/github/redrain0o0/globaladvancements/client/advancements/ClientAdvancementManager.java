package io.github.redrain0o0.globaladvancements.client.advancements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ClientAdvancementManager implements SimpleSynchronousResourceReloadListener {
    public static final ClientAdvancementManager INSTANCE = new ClientAdvancementManager();

    private static final String ADVANCEMENTS_FOLDER = "advancements";
    private static final Map<Identifier, ClientAdvancement> ADVANCEMENTS = new LinkedHashMap<>();
    private static final List<ClientAdvancement> ROOTS = new ArrayList<>();
    private static final Map<Identifier, List<ClientAdvancement>> CHILDREN = new LinkedHashMap<>();

    private ClientAdvancementManager() {}

    @Override
    public Identifier getFabricId() {
        return Globaladvancements.createId("client_advancements");
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        ADVANCEMENTS.clear();

        Map<Identifier, Resource> resources = resourceManager.listResources(
                ADVANCEMENTS_FOLDER,
                id -> id.getPath().endsWith(".json")
        );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();
            Resource resource = entry.getValue();
            Identifier advancementId = getAdvancementId(fileId);

            loadAdvancement(advancementId, resource).ifPresent((advancement) ->
                    ADVANCEMENTS.put(advancement.id(), advancement)
            );
        }

        buildTree();

        Globaladvancements.LOGGER.info("Loaded {} client advancements with {} roots", ADVANCEMENTS.size(), ROOTS.size());
        logTreeSummary();
    }

    public static Collection<ClientAdvancement> all() {
        return ADVANCEMENTS.values();
    }

    public static List<ClientAdvancement> roots() {
        return ROOTS;
    }

    public static List<ClientAdvancement> childrenOf(Identifier parentId) {
        return CHILDREN.getOrDefault(parentId, List.of());
    }

    public static int size() {
        return ADVANCEMENTS.size();
    }

    public static Optional<ClientAdvancement> get(Identifier id) {
        return Optional.ofNullable(ADVANCEMENTS.get(id));
    }

    private static void buildTree() {
        ROOTS.clear();
        CHILDREN.clear();

        for (ClientAdvancement advancement : ADVANCEMENTS.values()) {
            Optional<Identifier> parent = advancement.parent();

            if (parent.isEmpty()) {
                ROOTS.add(advancement);
                continue;
            }

            Identifier parentId = parent.get();
            if (!ADVANCEMENTS.containsKey(parentId)) {
                ROOTS.add(advancement);
                Globaladvancements.LOGGER.warn("Client advancement '{}' has missing parent '{}'", advancement.id(), parentId);
                continue;
            }

            CHILDREN.computeIfAbsent(parentId, id -> new ArrayList<>()).add(advancement);
        }
    }

    private static void logTreeSummary() {
        for (ClientAdvancement root : ROOTS) {
            Globaladvancements.LOGGER.info("Root '{}' has {} children", root.id(), childrenOf(root.id()).size());
        }
    }

    private static Optional<ClientAdvancement> loadAdvancement(Identifier advancementId, Resource resource) {
        try (BufferedReader reader = resource.openAsReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            Optional<Identifier> parent = getParent(json);
            Optional<DisplayInfo> display = getDisplay(advancementId, json);
            List<String> criterion = getCriterion(json);

            return Optional.of(new ClientAdvancement(advancementId, parent, display, criterion));
        } catch (IOException | RuntimeException exception) {
            Globaladvancements.LOGGER.warn("Failed to load client advancement '{}'", advancementId, exception);
            return Optional.empty();
        }
    }

    private static Identifier getAdvancementId(Identifier fileId) {
        String path = fileId.getPath();
        path = path.substring(ADVANCEMENTS_FOLDER.length() + 1);
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
        if (!json.has("display")) {
            return Optional.empty();
        }

        return DisplayInfo.CODEC.parse(JsonOps.INSTANCE, json.get("display")).resultOrPartial((error) ->
                Globaladvancements.LOGGER.warn("Failed to parse display for '{}': {}", advancementId, error)
        );
    }

    private static List<String> getCriterion(JsonObject json) {
        List<String> criterion = new ArrayList<>();

        if (!json.has("criterion")) {
            return criterion;
        }

        JsonArray criterionArray = json.getAsJsonArray("criterion");
        for (JsonElement criterionElement : criterionArray) {
            criterion.add(criterionElement.getAsString());
        }

        return criterion;
    }
}
