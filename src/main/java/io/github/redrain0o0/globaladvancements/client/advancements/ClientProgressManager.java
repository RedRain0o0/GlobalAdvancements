package io.github.redrain0o0.globaladvancements.client.advancements;

import com.google.gson.Gson;
import io.github.redrain0o0.globaladvancements.Globaladvancements;
import io.github.redrain0o0.globaladvancements.client.AdvancementsFile;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClientProgressManager {
    private static final Gson GSON = new Gson();
    private static final Set<String> completedCriteria = new LinkedHashSet<>();

    public static void load() {
        completedCriteria.clear();

        File file = getFile();
        if (!file.exists()) {
            save();
            return;
        }

        try (Reader reader = new FileReader(file)) {
            AdvancementsFile advancementsFile = GSON.fromJson(reader, AdvancementsFile.class);

            if (advancementsFile == null || advancementsFile.completedCriteria() == null) {
                return;
            }

            completedCriteria.addAll(advancementsFile.completedCriteria());
            Globaladvancements.LOGGER.info("Loaded {} completed client criteria", completedCriteria.size());
        } catch (IOException | RuntimeException exception) {
            Globaladvancements.LOGGER.warn("Failed to load client advancement progress", exception);
        }
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

    private static void save() {
        List<String> criteria = List.copyOf(completedCriteria);
        AdvancementsFile advancementsFile = new AdvancementsFile(List.of(), criteria, 4790);

        try (Writer writer = new FileWriter(getFile())) {
            GSON.toJson(advancementsFile, writer);
        } catch (IOException exception) {
            Globaladvancements.LOGGER.warn("Failed to save client advancement progress", exception);
        }
    }

    private static File getFile() {
        return new File(Minecraft.getInstance().gameDirectory, "advancements.json");
    }
}
