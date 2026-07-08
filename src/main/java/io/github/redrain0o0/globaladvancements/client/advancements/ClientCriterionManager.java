package io.github.redrain0o0.globaladvancements.client.advancements;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.network.ServerboundCriterionMappingsPayload.CriterionMapping;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ClientCriterionManager implements SimpleSynchronousResourceReloadListener {
    public static final ClientCriterionManager INSTANCE = new ClientCriterionManager();

    private static final String CRITERIA_FOLDER = "globaladvancements/criteria";
    private static final Map<Identifier, ClientCriterion> CRITERIA = new LinkedHashMap<>();

    private ClientCriterionManager() {}

    @Override
    public Identifier getFabricId() {
        return Globaladvancements.createId("client_criteria");
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        CRITERIA.clear();

        Map<Identifier, Resource> resources = resourceManager.listResources(
                CRITERIA_FOLDER,
                id -> id.getPath().endsWith(".json")
        );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier criterionId = getCriterionId(entry.getKey());
            loadCriterion(criterionId, entry.getValue()).ifPresent((criterion) ->
                    CRITERIA.put(criterion.id(), criterion)
            );
        }

        Globaladvancements.LOGGER.info("Loaded {} client criteria", CRITERIA.size());
    }

    public static Optional<CriterionMapping> getMapping(String criterionId, Identifier advancementId) {
        return Optional.ofNullable(CRITERIA.get(Identifier.parse(criterionId)))
                .map((criterion) -> new CriterionMapping(
                        criterion.advancementId(),
                        criterion.criterion(),
                        advancementId,
                        criterion.id().toString()
                ));
    }

    private static Optional<ClientCriterion> loadCriterion(Identifier criterionId, Resource resource) {
        try (BufferedReader reader = resource.openAsReader()) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            if (!"minecraft_advancement".equals(json.get("type").getAsString())) {
                Globaladvancements.LOGGER.warn("Client criterion '{}' has unsupported type '{}'", criterionId, json.get("type").getAsString());
                return Optional.empty();
            }

            Identifier advancementId = Identifier.parse(json.get("advancement").getAsString());
            String criterion = json.get("criterion").getAsString();
            return Optional.of(new ClientCriterion(criterionId, advancementId, criterion));
        } catch (IOException | RuntimeException exception) {
            Globaladvancements.LOGGER.warn("Failed to load client criterion '{}'", criterionId, exception);
            return Optional.empty();
        }
    }

    private static Identifier getCriterionId(Identifier fileId) {
        String path = fileId.getPath();
        path = path.substring(CRITERIA_FOLDER.length() + 1);
        path = path.substring(0, path.length() - ".json".length());

        return Identifier.fromNamespaceAndPath(fileId.getNamespace(), path);
    }

    private record ClientCriterion(Identifier id, Identifier advancementId, String criterion) {}
}
