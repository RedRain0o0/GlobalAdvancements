package io.github.redrain0o0.globaladvancements.mixin;

import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList.WorldListEntry;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WorldListEntry.class)
public interface WorldListEntryAccessor {
	@Accessor("icon")
	FaviconTexture gadva$getIcon();

	@Invoker("loadIcon")
	void gadva$callLoadIcon();

	@Mutable
	@Accessor("icon")
	void gadva$setIcon(FaviconTexture icon);

	@Accessor("summary")
	LevelSummary gadva$getSummary();
}
