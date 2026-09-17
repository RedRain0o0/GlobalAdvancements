package io.github.redrain0o0.globaladvancements.mixin.client;

import com.bawnorton.mixinsquared.TargetHandler;
import io.github.redrain0o0.globaladvancements.world.WorldCheats;
import net.minecraft.world.level.LevelSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelSettings.class, priority = 1500)
public class LegacyLevelSettingsMixin {
    @TargetHandler(mixin = "wily.legacy.mixin.base.client.ClientLevelSettingsMixin", name = "setAllowCommands")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"))
    private void gadva$markCheats(boolean allow, CallbackInfo ci) {
        if (allow) ((WorldCheats) this).markCheated();
    }
}
