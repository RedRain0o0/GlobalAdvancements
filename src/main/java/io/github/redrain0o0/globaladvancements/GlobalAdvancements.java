package io.github.redrain0o0.globaladvancements;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mojang.text2speech.Narrator.LOGGER;

public class GlobalAdvancements implements ModInitializer {

    public static final String MOD_ID = "globaladvancements";
    public static final Logger LOGGER = LoggerFactory.getLogger("Global Advancements");

    @Override
    public void onInitialize() {
        LOGGER.info("Initialising Global Advancements...");
        /*AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            BlockState state = world.getBlockState(pos);
            if (
                    state.requiresCorrectToolForDrops() &&
                            !player.isSpectator() &&
                            player.getMainHandItem().isEmpty()
            ) {
                LOGGER.info("This dummy aint gettin they item!");
            }
            return ActionResult.PASS;
        }); */
    }
}
