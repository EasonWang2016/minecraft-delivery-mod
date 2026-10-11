package com.minecrafteason.boiledegg;

public final class BoiledEggConfig {
    public static final String MOD_ID = "eason_delivery_fast";
    public static final String BOILED_EGG_ID = "boiled_egg";
    public static final String DELIVERY_PHONE_ID = "delivery_phone";
    public static final String MONEY_ID = "money";
    public static final int BOILED_EGG_NUTRITION = 4;
    public static final float BOILED_EGG_SATURATION = 0.3f;
    public static final int DELIVERY_PHONE_COOLDOWN_TICKS = 20 * 20;
    public static final int DELIVERY_DELAY_MILLIS = 4_000;
    public static final int DELIVERY_MONEY_COST_MIN = 2;
    public static final int DELIVERY_MONEY_COST_MAX = 3;
    public static final int DELIVERY_COURIER_DISTANCE_BLOCKS = 90;
    public static final double DELIVERY_COURIER_SPEED = 1.25;
    public static final double DELIVERY_HANDOFF_DISTANCE_BLOCKS = 3.0;
    public static final int DELIVERY_MONITOR_INTERVAL_MILLIS = 1_000;
    public static final int DELIVERY_MAX_MONITOR_STEPS = 60;
    public static final double DELIVERY_SAFE_SPAWN_Y_OFFSET = 0.1;

    private BoiledEggConfig() {
    }
}
