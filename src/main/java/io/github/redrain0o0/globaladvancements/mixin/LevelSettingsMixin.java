package io.github.redrain0o0.globaladvancements.mixin;

import com.mojang.serialization.Dynamic;
import io.github.redrain0o0.globaladvancements.world.WorldCheats;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelSettings.class)
public class LevelSettingsMixin implements WorldCheats {
    @Unique
    private volatile boolean gadva$cheated;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void gadva$markInitialCheats(String name, GameType gameType, LevelSettings.DifficultySettings difficulty,
                                         boolean allowCommands, WorldDataConfiguration configuration, CallbackInfo ci) {
        this.gadva$cheated = allowCommands || gameType == GameType.CREATIVE;
    }

    @Inject(method = "parse", at = @At("RETURN"))
    private static void gadva$loadCheats(Dynamic<?> data, WorldDataConfiguration configuration,
                                       CallbackInfoReturnable<LevelSettings> cir) {
        if (data.get(WorldCheats.TAG).asBoolean(false)) {
            ((WorldCheats) (Object) cir.getReturnValue()).markCheated();
        }
    }

    @Inject(method = {"withGameType", "withDifficulty", "withDifficultyLock", "withDataConfiguration", "copy"},
            at = @At("RETURN"))
    private void gadva$copyCheats(CallbackInfoReturnable<LevelSettings> cir) {
        if (this.gadva$cheated) {
            ((WorldCheats) (Object) cir.getReturnValue()).markCheated();
        }
    }

    @Override
    public boolean hasCheated() {
        return this.gadva$cheated;
    }

    @Override
    public void markCheated() {
        this.gadva$cheated = true;
    }
}
