package io.github.redrain0o0.globaladvancements.client.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ClientAdvancementView extends ClientAdvancements {
    private static final String COMPLETE_CRITERION = "complete";
    private static final DateTimeFormatter UNLOCK_TIME_FORMAT = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault());

    private ClientAdvancementView(Minecraft minecraft) {
        super(minecraft, new WorldSessionTelemetryManager(TelemetryEventSender.DISABLED, false, null, null));
    }

    public static ClientAdvancementView create() {
        ClientAdvancementView view = new ClientAdvancementView(Minecraft.getInstance());
        List<AdvancementHolder> holders = new ArrayList<>();
        Map<Identifier, AdvancementProgress> progress = new LinkedHashMap<>();

        for (ClientAdvancement advancement : ClientAdvancementManager.all()) {
            boolean unlocked = ClientProgressManager.isComplete(advancement);
            AdvancementRequirements requirements = advancement.requirements().isEmpty()
                    ? AdvancementRequirements.EMPTY
                    : new AdvancementRequirements(List.of(List.of(COMPLETE_CRITERION)));
            Advancement value = new Advancement(
                    advancement.parent(),
                    getDisplay(advancement, unlocked),
                    AdvancementRewards.EMPTY,
                    Map.of(),
                    requirements,
                    false
            );
            holders.add(new AdvancementHolder(advancement.id(), value));

            AdvancementProgress advancementProgress = new AdvancementProgress();
            advancementProgress.update(requirements);
            if (unlocked) {
                advancementProgress.grantProgress(COMPLETE_CRITERION);
            }
            progress.put(advancement.id(), advancementProgress);
        }

        view.update(new ClientboundUpdateAdvancementsPacket(true, holders, Set.of(), progress, false));
        for (AdvancementNode root : view.getTree().roots()) {
            TreeNodePosition.run(root);
        }
        return view;
    }

    private static Optional<DisplayInfo> getDisplay(ClientAdvancement advancement, boolean unlocked) {
        if (!unlocked) {
            return advancement.display();
        }

        return advancement.display().map(display -> ClientProgressManager.unlockedAt(advancement.id())
                .map(time -> withUnlockTime(display, UNLOCK_TIME_FORMAT.format(time)))
                .orElse(display));
    }

    private static DisplayInfo withUnlockTime(DisplayInfo display, String time) {
        Component description = display.getDescription().copy()
                .append("\n")
                .append(Component.translatable("gui.globaladvancements.unlockedAt", time));
        return new DisplayInfo(
                display.getIcon(),
                display.getTitle(),
                description,
                display.getBackground(),
                display.getType(),
                display.shouldShowToast(),
                display.shouldAnnounceChat(),
                display.isHidden()
        );
    }

    @Override
    public void setSelectedTab(@Nullable AdvancementHolder selectedTab, boolean tellServer) {
        super.setSelectedTab(selectedTab, false);
    }
}
