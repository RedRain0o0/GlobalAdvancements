package io.github.redrain0o0.globaladvancements.mixin.client;

import com.bawnorton.mixinsquared.TargetHandler;
import io.github.redrain0o0.globaladvancements.client.screen.LegacyGlobalAdvancementsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wily.legacy.client.screen.RenderableVList;

@Mixin(value = TitleScreen.class, priority = 1500)
public abstract class LegacyTitleScreenMixin extends Screen {
    @Shadow private @Final RenderableVList renderableVList;

    protected LegacyTitleScreenMixin(Component component) {
        super(component);
    }

    @TargetHandler(mixin = "wily.legacy.mixin.base.client.title.TitleScreenMixin", name = "rebuildMenuButtons")
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lwily/legacy/client/screen/RenderableVList;addRenderable(Lnet/minecraft/client/gui/components/Renderable;)Lwily/legacy/client/screen/RenderableVList;", ordinal = 4))
    private void gadva$addAdvancementsButton(CallbackInfo ci) {
        renderableVList.addRenderable(Button.builder(Component.translatable("gui.advancements"), button -> this.minecraft.setScreen(LegacyGlobalAdvancementsScreen.getActualAdvancementsScreenInstance(this))).build());
    }
}
