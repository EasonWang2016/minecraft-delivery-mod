package com.minecrafteason.boiledegg;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.api.ModInitializer;

public final class BoiledEggMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ModItems.initialize();
        CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register((tab, output) -> {
            output.accept(ModItems.BOILED_EGG);
            output.accept(ModItems.DELIVERY_PHONE);
            output.accept(ModItems.MONEY);
        });
    }
}
