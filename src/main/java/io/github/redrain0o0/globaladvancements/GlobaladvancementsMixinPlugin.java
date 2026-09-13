package io.github.redrain0o0.globaladvancements;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

public class GlobaladvancementsMixinPlugin implements IMixinConfigPlugin {
    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !mixinClassName.equals("io.github.redrain0o0.globaladvancements.mixin.client.LegacyTitleScreenMixin") || FabricLoader.getInstance().isModLoaded("legacy");
    }

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        for (MethodNode method : targetClass.methods) {
            if (Modifier.isAbstract(method.access) || Modifier.isNative(method.access)) continue;
            InsnList abstractInsnNodes = new InsnList();
            abstractInsnNodes.add(new FieldInsnNode(Opcodes.GETSTATIC, "io/github/redrain0o0/globaladvancements/Globaladvancements", "LOGGER", "Lorg/slf4j/Logger;"));
            abstractInsnNodes.add(new LdcInsnNode("tung tung tung sahur"));
            abstractInsnNodes.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "org/slf4j/Logger", "info", "(Ljava/lang/String;)V"));
            method.instructions.insert(abstractInsnNodes);
        }
    }
}
