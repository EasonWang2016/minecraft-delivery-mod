package com.minecrafteason.boiledegg;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

public final class ModItemIds {
    public static final ResourceKey<Item> BOILED_EGG = create(BoiledEggConfig.BOILED_EGG_ID);
    public static final ResourceKey<Item> DELIVERY_PHONE = create(BoiledEggConfig.DELIVERY_PHONE_ID);
    public static final ResourceKey<Item> MONEY = create(BoiledEggConfig.MONEY_ID);

    private ModItemIds() {
    }

    private static ResourceKey<Item> create(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(BoiledEggConfig.MOD_ID, name));
    }
}
