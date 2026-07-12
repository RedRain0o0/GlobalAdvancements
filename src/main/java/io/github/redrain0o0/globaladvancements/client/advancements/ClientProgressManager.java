package io.github.redrain0o0.globaladvancements.client.advancements;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ClientProgressManager {
    private static final Gson GSON = new Gson();
    private static final Map<Identifier, Set<String>> completedCriteria = new LinkedHashMap<>();
    private static final Map<Identifier, Instant> unlockTimes = new LinkedHashMap<>();

    public static void load() {
        completedCriteria.clear();
        unlockTimes.clear();

        JsonObject file = readFile();
        JsonObject advancements = file.getAsJsonObject("completed_criteria");

        if (advancements == null) {
            save();
            return;
        }

        int criteriaCount = 0;
        for (Map.Entry<String, JsonElement> advancement : advancements.entrySet()) {
            if (!advancement.getValue().isJsonArray()) continue;

            Set<String> criteria = completedCriteria.computeIfAbsent(Identifier.parse(advancement.getKey()), id -> new LinkedHashSet<>());
            for (JsonElement criterion : advancement.getValue().getAsJsonArray()) {
                if (criteria.add(criterion.getAsString())) criteriaCount++;
            }
        }

        JsonObject unlockedAt = file.getAsJsonObject("unlocked_at");
        if (unlockedAt != null) {
            for (Map.Entry<String, JsonElement> entry : unlockedAt.entrySet()) {
                try {
                    unlockTimes.put(Identifier.parse(entry.getKey()), Instant.parse(entry.getValue().getAsString()));
                } catch (RuntimeException exception) {
                    Globaladvancements.LOGGER.warn("Ignoring invalid unlock time for '{}'", entry.getKey(), exception);
                }
            }
        }

        Globaladvancements.LOGGER.info("Loaded {} completed client criteria and {} unlock times", criteriaCount, unlockTimes.size());
    }

    public static boolean completeCriterion(ClientAdvancement advancement, String criterion) {
        boolean wasComplete = isComplete(advancement);
        Set<String> criteria = completedCriteria.computeIfAbsent(advancement.id(), id -> new LinkedHashSet<>());
        if (!criteria.add(criterion)) {
            return false;
        }

        if (!wasComplete && isComplete(advancement)) {
            unlockTimes.putIfAbsent(advancement.id(), Instant.now());
        }

        save();
        return true;
    }

    public static Optional<Instant> unlockedAt(Identifier advancementId) {
        return Optional.ofNullable(unlockTimes.get(advancementId));
    }

    public static boolean isComplete(ClientAdvancement advancement) {
        Set<String> criteria = completedCriteria.get(advancement.id());
        if (criteria == null || advancement.requirements().isEmpty()) {
            return false;
        }

        for (List<String> requirement : advancement.requirements()) {
            if (criteria.containsAll(requirement)) {
                return true;
            }
        }

        return false;
    }

    public static void save() {
        JsonObject advancementsFile = new JsonObject();
        JsonObject advancements = new JsonObject();

        for (Map.Entry<Identifier, Set<String>> advancement : completedCriteria.entrySet()) {
            JsonArray criteria = new JsonArray();
            for (String criterion : advancement.getValue()) {
                criteria.add(criterion);
            }
            advancements.add(advancement.getKey().toString(), criteria);
        }

        JsonObject unlockedAt = new JsonObject();
        for (Map.Entry<Identifier, Instant> entry : unlockTimes.entrySet()) {
            unlockedAt.addProperty(entry.getKey().toString(), entry.getValue().toString());
        }

        advancementsFile.add("advancements", new JsonArray());
        advancementsFile.add("completed_criteria", advancements);
        advancementsFile.add("unlocked_at", unlockedAt);
        advancementsFile.addProperty("dataVersion", 4790);

        try (Writer writer = new FileWriter(getFile())) {
            GSON.toJson(advancementsFile, writer);
        } catch (IOException exception) {
            Globaladvancements.LOGGER.warn("Failed to save client advancement progress", exception);
        }
    }

    private static JsonObject readFile() {
        File file = getFile();
        if (!file.exists() || file.length() == 0) {
            return new JsonObject();
        }

        try (Reader reader = new FileReader(file)) {
            JsonElement fileContents = JsonParser.parseReader(reader);
            if (fileContents.isJsonObject()) {
                return fileContents.getAsJsonObject();
            }
        } catch (IOException | RuntimeException exception) {
            Globaladvancements.LOGGER.warn("Failed to read client advancement progress", exception);
        }

        return new JsonObject();
    }

    private static File getFile() {
        return new File(Minecraft.getInstance().gameDirectory, "advancements.json");
    }
}
