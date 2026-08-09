package io.github.redrain0o0.globaladvancements.client.advancements;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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
    private static boolean ignoreExistingVanillaProgress;
    private static int tamedWolves;

    public static void load() {
        completedCriteria.clear();
        unlockTimes.clear();
        ignoreExistingVanillaProgress = false;
        tamedWolves = 0;

        JsonObject file = readFile(getFile());
        JsonObject advancements = getObject(file, "completed_criteria");

        if (advancements == null) {
            save();
            return;
        }

        int criteriaCount = 0;
        for (Map.Entry<String, JsonElement> advancement : advancements.entrySet()) {
            if (!advancement.getValue().isJsonArray()) {
                continue;
            }

            try {
                Identifier advancementId = Identifier.parse(advancement.getKey());
                Set<String> criteria = completedCriteria.computeIfAbsent(advancementId, id -> new LinkedHashSet<>());
                for (JsonElement criterion : advancement.getValue().getAsJsonArray()) {
                    if (criteria.add(criterion.getAsString())) {
                        criteriaCount++;
                    }
                }
            } catch (RuntimeException exception) {
                Globaladvancements.LOGGER.warn("Ignoring invalid client advancement progress for '{}'", advancement.getKey(), exception);
            }
        }

        JsonObject unlockedAt = getObject(file, "unlocked_at");
        if (unlockedAt != null) {
            for (Map.Entry<String, JsonElement> entry : unlockedAt.entrySet()) {
                try {
                    unlockTimes.put(Identifier.parse(entry.getKey()), Instant.parse(entry.getValue().getAsString()));
                } catch (RuntimeException exception) {
                    Globaladvancements.LOGGER.warn("Ignoring invalid unlock time for '{}'", entry.getKey(), exception);
                }
            }
        }

        JsonElement ignoreExisting = file.get("ignore_existing_vanilla_progress");
        if (ignoreExisting != null && ignoreExisting.isJsonPrimitive() && ignoreExisting.getAsJsonPrimitive().isBoolean()) {
            ignoreExistingVanillaProgress = ignoreExisting.getAsBoolean();
        }

        JsonElement tamedWolvesElement = file.get("tamed_wolves");
        if (tamedWolvesElement != null && tamedWolvesElement.isJsonPrimitive()
                && tamedWolvesElement.getAsJsonPrimitive().isNumber()) {
            tamedWolves = Math.max(0, tamedWolvesElement.getAsInt());
        }

        Globaladvancements.LOGGER.info("Loaded {} completed client criteria and {} unlock times", criteriaCount, unlockTimes.size());
    }

    public static boolean completeCriterion(ClientAdvancement advancement, String criterion) {
        return completeCriterion(advancement, criterion, true);
    }

    static boolean completeCriterion(ClientAdvancement advancement, String criterion, boolean saveProgress) {
        boolean wasComplete = isComplete(advancement);
        Set<String> criteria = completedCriteria.computeIfAbsent(advancement.id(), id -> new LinkedHashSet<>());
        if (!criteria.add(criterion)) {
            return false;
        }

        boolean completed = !wasComplete && meetsRequirements(advancement);
        if (completed) {
            unlockTimes.putIfAbsent(advancement.id(), Instant.now());
        }

        if (saveProgress) {
            save();
        }
        return completed;
    }

    static int recordTamedWolf() {
        tamedWolves++;
        save();
        return tamedWolves;
    }

    public static Optional<Instant> unlockedAt(Identifier advancementId) {
        return Optional.ofNullable(unlockTimes.get(advancementId));
    }

    public static Set<String> completedCriteria(Identifier advancementId) {
        return Set.copyOf(completedCriteria.getOrDefault(advancementId, Set.of()));
    }

    public static boolean isComplete(ClientAdvancement advancement) {
        return unlockTimes.containsKey(advancement.id()) || meetsRequirements(advancement);
    }

    static void preserveCompletions(Iterable<ClientAdvancement> advancements) {
        boolean changed = false;
        Instant now = Instant.now();
        for (ClientAdvancement advancement : advancements) {
            if (!unlockTimes.containsKey(advancement.id()) && meetsRequirements(advancement)) {
                unlockTimes.put(advancement.id(), now);
                changed = true;
            }
        }
        if (changed) {
            save();
        }
    }

    private static boolean meetsRequirements(ClientAdvancement advancement) {
        Set<String> criteria = completedCriteria.get(advancement.id());
        if (criteria == null || advancement.requirements().isEmpty()) {
            return false;
        }

        for (List<String> requirement : advancement.requirements()) {
            boolean groupComplete = false;
            for (String criterion : requirement) {
                if (criteria.contains(criterion)) {
                    groupComplete = true;
                    break;
                }
            }

            if (!groupComplete) {
                return false;
            }
        }

        return true;
    }

    public static void save() {
        writeFile(createProgressFile(completedCriteria, unlockTimes, tamedWolves, ignoreExistingVanillaProgress), getFile());
    }

    public static boolean resetWithBackup() {
        if (!writeFile(createProgressFile(completedCriteria, unlockTimes, tamedWolves, ignoreExistingVanillaProgress), getBackupFile())) {
            return false;
        }
        if (!writeFile(createProgressFile(Map.of(), Map.of(), 0, true), getFile())) {
            return false;
        }

        completedCriteria.clear();
        unlockTimes.clear();
        tamedWolves = 0;
        ignoreExistingVanillaProgress = true;
        return true;
    }

    public static boolean restoreBackup() {
        JsonObject backup = readFile(getBackupFile());
        if (!isValidProgressFile(backup) || !writeFile(backup, getFile())) {
            return false;
        }

        load();
        if (!ignoreExistingVanillaProgress) {
            ClientCriterionManager.clearMinecraftAdvancements();
        }
        return true;
    }

    public static boolean hasBackup() {
        return getBackupFile().isFile();
    }

    public static boolean shouldIgnoreExistingVanillaProgress() {
        return ignoreExistingVanillaProgress;
    }

    private static JsonObject createProgressFile(Map<Identifier, ? extends Set<String>> criteriaByAdvancement,
                                                 Map<Identifier, Instant> timesByAdvancement,
                                                 int tamedWolves,
                                                 boolean ignoreExistingProgress) {
        JsonObject advancementsFile = new JsonObject();
        JsonObject advancements = new JsonObject();

        for (Map.Entry<Identifier, ? extends Set<String>> advancement : criteriaByAdvancement.entrySet()) {
            JsonArray criteria = new JsonArray();
            for (String criterion : advancement.getValue()) {
                criteria.add(criterion);
            }
            advancements.add(advancement.getKey().toString(), criteria);
        }

        JsonObject unlockedAt = new JsonObject();
        for (Map.Entry<Identifier, Instant> entry : timesByAdvancement.entrySet()) {
            unlockedAt.addProperty(entry.getKey().toString(), entry.getValue().toString());
        }

        advancementsFile.add("advancements", new JsonArray());
        advancementsFile.add("completed_criteria", advancements);
        advancementsFile.add("unlocked_at", unlockedAt);
        advancementsFile.addProperty("ignore_existing_vanilla_progress", ignoreExistingProgress);
        advancementsFile.addProperty("tamed_wolves", tamedWolves);
        advancementsFile.addProperty("dataVersion", 4790);
        return advancementsFile;
    }

    private static boolean writeFile(JsonObject progress, File file) {
        Path destination = file.toPath().toAbsolutePath();
        Path temporary = null;
        try {
            Path directory = destination.getParent();
            if (directory != null) {
                Files.createDirectories(directory);
            }
            temporary = Files.createTempFile(directory, destination.getFileName().toString(), ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(progress, writer);
            }

            try {
                Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            temporary = null;
            return true;
        } catch (IOException exception) {
            Globaladvancements.LOGGER.warn("Failed to save client advancement progress", exception);
            return false;
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException exception) {
                    Globaladvancements.LOGGER.warn("Failed to remove temporary client advancement progress file", exception);
                }
            }
        }
    }

    private static JsonObject readFile(File file) {
        if (!file.exists() || file.length() == 0) {
            return new JsonObject();
        }

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            JsonElement fileContents = JsonParser.parseReader(reader);
            if (fileContents.isJsonObject()) {
                return fileContents.getAsJsonObject();
            }
        } catch (IOException | RuntimeException exception) {
            Globaladvancements.LOGGER.warn("Failed to read client advancement progress", exception);
        }

        return new JsonObject();
    }

    private static boolean isValidProgressFile(JsonObject file) {
        JsonObject advancements = getObject(file, "completed_criteria");
        JsonObject unlockedAt = getObject(file, "unlocked_at");
        if (advancements == null || unlockedAt == null) {
            return false;
        }

        JsonElement ignoreExisting = file.get("ignore_existing_vanilla_progress");
        if (ignoreExisting != null && (!ignoreExisting.isJsonPrimitive() || !ignoreExisting.getAsJsonPrimitive().isBoolean())) {
            return false;
        }

        JsonElement tamedWolves = file.get("tamed_wolves");
        if (tamedWolves != null && (!tamedWolves.isJsonPrimitive()
                || !tamedWolves.getAsJsonPrimitive().isNumber()
                || tamedWolves.getAsInt() < 0)) {
            return false;
        }

        try {
            for (Map.Entry<String, JsonElement> advancement : advancements.entrySet()) {
                Identifier.parse(advancement.getKey());
                if (!advancement.getValue().isJsonArray()) {
                    return false;
                }
                for (JsonElement criterion : advancement.getValue().getAsJsonArray()) {
                    if (!criterion.isJsonPrimitive() || !criterion.getAsJsonPrimitive().isString()) {
                        return false;
                    }
                }
            }
            for (Map.Entry<String, JsonElement> entry : unlockedAt.entrySet()) {
                Identifier.parse(entry.getKey());
                Instant.parse(entry.getValue().getAsString());
            }
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static File getFile() {
        return new File(Minecraft.getInstance().gameDirectory, "advancements.json");
    }

    private static File getBackupFile() {
        return new File(Minecraft.getInstance().gameDirectory, "advancements.backup.json");
    }

    private static JsonObject getObject(JsonObject object, String name) {
        JsonElement element = object.get(name);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }
}
