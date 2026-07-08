package io.github.redrain0o0.globaladvancements.client.advancements;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.redrain0o0.globaladvancements.Globaladvancements;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Set;

public class ClientProgressManager {
    private static final Gson GSON = new Gson();
    private static final Set<String> completedCriteria = new LinkedHashSet<>();

    public static void load() {
        completedCriteria.clear();

        JsonElement criteria = readFile().get("completed_criteria");

        if (criteria == null || !criteria.isJsonArray()) {
            save();
            return;
        }

        for (JsonElement criterion : criteria.getAsJsonArray()) {
            completedCriteria.add(criterion.getAsString());
        }
        Globaladvancements.LOGGER.info("Loaded {} completed client criteria", completedCriteria.size());
    }

    public static boolean completeCriterion(String criterion) {
        if (!completedCriteria.add(criterion)) {
            return false;
        }

        save();
        return true;
    }

    public static boolean isComplete(ClientAdvancement advancement) {
        return !advancement.criterion().isEmpty() && completedCriteria.containsAll(advancement.criterion());
    }

    public static void save() {
        JsonObject advancementsFile = readFile();
        JsonArray criteria = new JsonArray();

        for (String criterion : completedCriteria) {
            criteria.add(criterion);
        }

        advancementsFile.add("completed_criteria", criteria);
        if (!advancementsFile.has("advancements")) {
            advancementsFile.add("advancements", new JsonArray());
        }
        if (!advancementsFile.has("dataVersion")) {
            advancementsFile.addProperty("dataVersion", 4790);
        }

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
