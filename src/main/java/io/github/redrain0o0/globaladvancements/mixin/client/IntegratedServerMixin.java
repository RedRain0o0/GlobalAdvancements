package io.github.redrain0o0.globaladvancements.mixin.client;

import io.github.redrain0o0.globaladvancements.world.WorldCheats;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {
    @Inject(method = "publishServer", at = @At("RETURN"))
    private void gadva$markLanCheats(GameType gameType, boolean commands, int port, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && (commands || gameType == GameType.CREATIVE)) {
            ((WorldCheats) (Object) ((IntegratedServer) (Object) this).getWorldData().getLevelSettings()).markCheated();
        }
    }
}
