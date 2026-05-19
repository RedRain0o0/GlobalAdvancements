package io.github.redrain0o0.globaladvancements.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.redrain0o0.globaladvancements.client.screen.GlobalAdvancementsScreen;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component component) {
        super(component);
    }

    @Definition(id = "l", field = "Lnet/minecraft/client/gui/screens/TitleScreen;l:I")
    @Expression("? + 36")
    @ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int gadva$giveMeL(int original, @Share("arg") LocalIntRef lRef) {
        lRef.set(original);
        return original;
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void gadva$init(CallbackInfo ci, @Share("arg") LocalIntRef lRef) {
        int l = lRef.get();
        SpriteIconButton advancementsIconButton = (SpriteIconButton)this.addRenderableWidget(SpriteIconButton.builder(Component.translatable("gui.advancements"), button -> this.minecraft.setScreen(new GlobalAdvancementsScreen(this)), true).width(20).sprite(Identifier.withDefaultNamespace("icon/accessibility"), 15, 15).build());
        advancementsIconButton.setPosition(this.width / 2 - 124, l - 36);
        //SpriteIconButton statsIconButton = (SpriteIconButton)this.addRenderableWidget(SpriteIconButton.builder(Component.translatable("gui.stats"), button -> this.minecraft.setScreen(new GlobalStatsScreen(this)), true).width(20).sprite(Identifier.withDefaultNamespace("icon/accessibility"), 15, 15).build());
        //statsIconButton.setPosition(this.width / 2 + 104, l - 36);
    }
}
