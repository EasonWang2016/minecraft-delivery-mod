package com.minecrafteason.boiledegg;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumables;

public final class ModItems {
    private static final FoodProperties BOILED_EGG_FOOD = new FoodProperties.Builder()
            .nutrition(BoiledEggConfig.BOILED_EGG_NUTRITION)
            .saturationModifier(BoiledEggConfig.BOILED_EGG_SATURATION)
            .build();

    public static final Item BOILED_EGG = register(
            ModItemIds.BOILED_EGG,
            Item::new,
            new Item.Properties().food(BOILED_EGG_FOOD, Consumables.defaultFood().build())
    );

    public static final Item DELIVERY_PHONE = register(
            ModItemIds.DELIVERY_PHONE,
            DeliveryPhoneItem::new,
            new Item.Properties().stacksTo(1)
    );

    public static final Item MONEY = register(
            ModItemIds.MONEY,
            Item::new,
            new Item.Properties()
    );

    private ModItems() {
    }

    public static void initialize() {
    }

    private static Item register(ResourceKey<Item> itemKey, Function<Item.Properties, Item> itemFactory, Item.Properties settings) {
        Item item = itemFactory.apply(settings.setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, item);
        return item;
    }
}
