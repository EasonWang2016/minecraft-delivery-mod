package com.minecrafteason.boiledegg;

import java.nio.file.Files;
import java.nio.file.Path;

public final class BoiledEggProjectTest {
    public static void main(String[] args) throws Exception {
        assertEquals("eason_delivery_fast", BoiledEggConfig.MOD_ID, "mod id");
        assertEquals("boiled_egg", BoiledEggConfig.BOILED_EGG_ID, "item id");
        assertEquals("delivery_phone", BoiledEggConfig.DELIVERY_PHONE_ID, "delivery phone id");
        assertEquals("money", BoiledEggConfig.MONEY_ID, "money item id");
        assertEquals(4, BoiledEggConfig.BOILED_EGG_NUTRITION, "nutrition");
        assertEquals(20 * 20, BoiledEggConfig.DELIVERY_PHONE_COOLDOWN_TICKS, "delivery phone cooldown");
        assertEquals(2, BoiledEggConfig.DELIVERY_MONEY_COST_MIN, "minimum money cost");
        assertEquals(3, BoiledEggConfig.DELIVERY_MONEY_COST_MAX, "maximum money cost");
        assertEquals(90, BoiledEggConfig.DELIVERY_COURIER_DISTANCE_BLOCKS, "courier spawn distance");
        assertEquals(1.25, BoiledEggConfig.DELIVERY_COURIER_SPEED, "courier walking speed");
        assertEquals(60, BoiledEggConfig.DELIVERY_MAX_MONITOR_STEPS, "maximum delivery seconds");
        assertEquals(0.1, BoiledEggConfig.DELIVERY_SAFE_SPAWN_Y_OFFSET, "safe spawn y offset");

        Path recipePath = Path.of("src/main/resources/data/eason_delivery_fast/recipe/boiled_egg_from_smelting.json");
        String recipe = Files.readString(recipePath);

        assertContains(recipe, "\"type\": \"minecraft:smelting\"");
        assertContains(recipe, "\"ingredient\": \"#minecraft:eggs\"");
        assertContains(recipe, "\"id\": \"eason_delivery_fast:boiled_egg\"");

        Path zhCnPath = Path.of("src/main/resources/assets/eason_delivery_fast/lang/zh_cn.json");
        String zhCn = Files.readString(zhCnPath);
        assertContains(zhCn, "\"item.eason_delivery_fast.delivery_phone\": \"疾速外卖手机\"");
        assertContains(zhCn, "\"item.eason_delivery_fast.money\": \"钱\"");

        Path phoneModelPath = Path.of("src/main/resources/assets/eason_delivery_fast/models/item/delivery_phone.json");
        String phoneModel = Files.readString(phoneModelPath);
        assertContains(phoneModel, "\"layer0\": \"eason_delivery_fast:item/delivery_phone\"");
        assertTrue(Files.exists(Path.of("src/main/resources/assets/eason_delivery_fast/textures/item/delivery_phone.png")), "delivery phone texture exists");

        Path phoneRecipePath = Path.of("src/main/resources/data/eason_delivery_fast/recipe/delivery_phone.json");
        String phoneRecipe = Files.readString(phoneRecipePath);
        assertContains(phoneRecipe, "\"type\": \"minecraft:crafting_shapeless\"");
        assertContains(phoneRecipe, "\"minecraft:iron_ingot\"");
        assertContains(phoneRecipe, "\"minecraft:redstone\"");
        assertTrue(!phoneRecipe.contains("minecraft:glass_pane"), "delivery phone recipe does not use glass pane");
        assertTrue(!Files.exists(Path.of("src/main/resources/data/eason_delivery_fast/recipe/delivery_phone_shapeless.json")), "old shapeless phone recipe removed");
        assertTrue(!Files.exists(Path.of("src/main/resources/data/eason_delivery_fast/recipe/delivery_phone_from_iron_ingot.json")), "iron-only phone recipe removed");

        Path moneyRecipePath = Path.of("src/main/resources/data/eason_delivery_fast/recipe/money_from_smelting.json");
        String moneyRecipe = Files.readString(moneyRecipePath);
        assertContains(moneyRecipe, "\"type\": \"minecraft:smelting\"");
        assertContains(moneyRecipe, "\"ingredient\": \"minecraft:iron_nugget\"");
        assertContains(moneyRecipe, "\"id\": \"eason_delivery_fast:money\"");
        assertTrue(Files.exists(Path.of("src/main/resources/assets/eason_delivery_fast/items/money.json")), "money item definition exists");
        assertTrue(Files.exists(Path.of("src/main/resources/assets/eason_delivery_fast/models/item/money.json")), "money model exists");

        String modInitializer = Files.readString(Path.of("src/main/java/com/minecrafteason/boiledegg/BoiledEggMod.java"));
        assertContains(modInitializer, "CreativeModeTabEvents.MODIFY_OUTPUT_ALL");
        assertContains(modInitializer, "ModItems.DELIVERY_PHONE");
        assertContains(modInitializer, "ModItems.MONEY");
    }

    private static void assertEquals(Object expected, Object actual, String label) {
        if (!expected.equals(actual)) {
            throw new AssertionError(label + " expected " + expected + " but got " + actual);
        }
    }

    private static void assertContains(String text, String expected) {
        if (!text.contains(expected)) {
            throw new AssertionError("Expected to find: " + expected);
        }
    }

    private static void assertTrue(boolean value, String label) {
        if (!value) {
            throw new AssertionError(label);
        }
    }
}
