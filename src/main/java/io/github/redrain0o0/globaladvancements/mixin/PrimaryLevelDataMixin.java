package io.github.redrain0o0.globaladvancements.mixin;

import io.github.redrain0o0.globaladvancements.world.WorldCheats;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PrimaryLevelData.class)
public class PrimaryLevelDataMixin {
    @Shadow
    private LevelSettings settings;

    @Inject(method = "setTagData", at = @At("TAIL"))
    private void gadva$saveCheats(CompoundTag tag, UUID playerId, CallbackInfo ci) {
        tag.putBoolean(WorldCheats.TAG, WorldCheats.isCheated(this.settings));
    }
}
