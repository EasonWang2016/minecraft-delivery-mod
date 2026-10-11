package com.minecrafteason.boiledegg;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public final class DeliveryPhoneItem extends Item {
    private static final List<Item> DELIVERY_ITEMS = List.of(
            ModItems.BOILED_EGG,
            Items.BREAD,
            Items.COOKED_BEEF,
            Items.COOKED_CHICKEN
    );

    public DeliveryPhoneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack phone = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        if (serverPlayer.getCooldowns().isOnCooldown(phone)) {
            serverPlayer.sendSystemMessage(Component.literal("外卖史蒂夫还在路上，稍等一下。"));
            return InteractionResult.CONSUME;
        }

        int moneyCost = ThreadLocalRandom.current().nextInt(
                BoiledEggConfig.DELIVERY_MONEY_COST_MIN,
                BoiledEggConfig.DELIVERY_MONEY_COST_MAX + 1
        );
        if (!tryPayMoneyCost(serverPlayer, moneyCost)) {
            serverPlayer.sendSystemMessage(Component.literal("需要 " + moneyCost + " 个钱才能点外卖。"));
            return InteractionResult.CONSUME;
        }

        serverPlayer.getCooldowns().addCooldown(phone, BoiledEggConfig.DELIVERY_PHONE_COOLDOWN_TICKS);
        spawnCourier(serverPlayer);
        serverPlayer.sendSystemMessage(Component.literal("已支付 " + moneyCost + " 个钱，外卖史蒂夫正在配送..."));
        return InteractionResult.SUCCESS_SERVER;
    }

    private static boolean tryPayMoneyCost(ServerPlayer player, int moneyCost) {
        int availableMoney = countItem(player, ModItems.MONEY);
        if (availableMoney < moneyCost) {
            return false;
        }

        removeItems(player, ModItems.MONEY, moneyCost);
        return true;
    }

    private static int countItem(ServerPlayer player, Item item) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }

        return count;
    }

    private static void removeItems(ServerPlayer player, Item item, int count) {
        int remaining = count;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(item)) {
                continue;
            }

            int removed = Math.min(remaining, stack.getCount());
            player.getInventory().removeItem(slot, removed);
            remaining -= removed;
        }
    }

    private static void spawnCourier(ServerPlayer player) {
        ServerLevel level = player.level();
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(
                Identifier.fromNamespaceAndPath("minecraft", "villager")
        );

        if (!(entityType.create(level, EntitySpawnReason.COMMAND) instanceof Villager courier)) {
            deliverWithoutCourier(player);
            return;
        }

        Vec3 spawnPosition = safeGroundPositionBehindPlayer(player, BoiledEggConfig.DELIVERY_COURIER_DISTANCE_BLOCKS);
        courier.setPos(spawnPosition.x, spawnPosition.y, spawnPosition.z);
        courier.setCustomName(Component.literal("外卖史蒂夫"));
        courier.setCustomNameVisible(true);
        courier.setPersistenceRequired();
        courier.setPermanentlyInvulnerable(true);
        level.addFreshEntity(courier);
        courier.getNavigation().moveTo(player, BoiledEggConfig.DELIVERY_COURIER_SPEED);

        monitorDelivery(player, courier);
    }

    private static Vec3 positionBehindPlayer(ServerPlayer player, double distance) {
        Vec3 direction = horizontalDirection(player.getLookAngle()).scale(-1.0);
        return player.position().add(direction.scale(distance));
    }

    private static Vec3 safeGroundPositionBehindPlayer(ServerPlayer player, double distance) {
        ServerLevel level = player.level();
        Vec3 flatPosition = positionBehindPlayer(player, distance);
        int x = (int) Math.round(flatPosition.x);
        int z = (int) Math.round(flatPosition.z);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new Vec3(x + 0.5, y + BoiledEggConfig.DELIVERY_SAFE_SPAWN_Y_OFFSET, z + 0.5);
    }

    private static Vec3 exitPosition(ServerPlayer player, Mob courier, double distance) {
        Vec3 direction = new Vec3(courier.getX() - player.getX(), 0.0, courier.getZ() - player.getZ());
        if (direction.lengthSqr() < 0.01) {
            direction = horizontalDirection(player.getLookAngle()).scale(-1.0);
        } else {
            direction = direction.normalize();
        }

        return player.position().add(direction.scale(distance));
    }

    private static Vec3 horizontalDirection(Vec3 direction) {
        Vec3 horizontal = new Vec3(direction.x, 0.0, direction.z);
        if (horizontal.lengthSqr() < 0.01) {
            return new Vec3(1.0, 0.0, 0.0);
        }

        return horizontal.normalize();
    }

    private static void deliverWithoutCourier(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        Thread deliveryThread = new Thread(() -> {
            try {
                Thread.sleep(BoiledEggConfig.DELIVERY_DELAY_MILLIS);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                return;
            }

            server.executeIfPossible(() -> giveFoodDirectly(player));
        }, "boiled-egg-delivery");
        deliveryThread.setDaemon(true);
        deliveryThread.start();
    }

    private static void monitorDelivery(ServerPlayer player, Mob courier) {
        MinecraftServer server = player.level().getServer();
        Thread deliveryThread = new Thread(() -> {
            boolean delivered = false;
            Vec3 exitTarget = null;

            for (int step = 0; step < BoiledEggConfig.DELIVERY_MAX_MONITOR_STEPS; step++) {
                try {
                    Thread.sleep(BoiledEggConfig.DELIVERY_MONITOR_INTERVAL_MILLIS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }

                final boolean hasDelivered = delivered;
                final Vec3 currentExitTarget = exitTarget;
                DeliveryStepResult[] result = new DeliveryStepResult[1];
                server.executeBlocking(() -> result[0] = deliveryStep(player, courier, hasDelivered, currentExitTarget));

                if (result[0].finished()) {
                    return;
                }

                delivered = result[0].delivered();
                exitTarget = result[0].exitTarget();
            }

            final boolean hasDelivered = delivered;
            server.executeIfPossible(() -> completeTimedOutDelivery(player, courier, hasDelivered));
        }, "boiled-egg-delivery");
        deliveryThread.setDaemon(true);
        deliveryThread.start();
    }

    private static DeliveryStepResult deliveryStep(ServerPlayer player, Mob courier, boolean delivered, Vec3 exitTarget) {
        if (player.isRemoved()) {
            courier.discard();
            return new DeliveryStepResult(delivered, exitTarget, true);
        }

        if (courier.isRemoved()) {
            return new DeliveryStepResult(delivered, exitTarget, true);
        }

        if (!delivered) {
            courier.getNavigation().moveTo(player, BoiledEggConfig.DELIVERY_COURIER_SPEED);

            double handoffDistance = BoiledEggConfig.DELIVERY_HANDOFF_DISTANCE_BLOCKS;
            if (courier.distanceToSqr(player) > handoffDistance * handoffDistance) {
                return new DeliveryStepResult(false, null, false);
            }

            throwFoodToPlayer(player, courier);
            player.sendSystemMessage(Component.literal("外卖史蒂夫：您的外卖到了！"));
            Vec3 target = exitPosition(player, courier, BoiledEggConfig.DELIVERY_COURIER_DISTANCE_BLOCKS);
            courier.getNavigation().moveTo(target.x, target.y, target.z, BoiledEggConfig.DELIVERY_COURIER_SPEED);
            return new DeliveryStepResult(true, target, false);
        }

        if (exitTarget == null) {
            exitTarget = exitPosition(player, courier, BoiledEggConfig.DELIVERY_COURIER_DISTANCE_BLOCKS);
        }

        courier.getNavigation().moveTo(exitTarget.x, exitTarget.y, exitTarget.z, BoiledEggConfig.DELIVERY_COURIER_SPEED);
        double vanishDistance = BoiledEggConfig.DELIVERY_COURIER_DISTANCE_BLOCKS;
        if (courier.distanceToSqr(player) >= vanishDistance * vanishDistance) {
            courier.discard();
            return new DeliveryStepResult(true, exitTarget, true);
        }

        return new DeliveryStepResult(true, exitTarget, false);
    }

    private static void throwFoodToPlayer(ServerPlayer player, Mob courier) {
        Item item = DELIVERY_ITEMS.get(ThreadLocalRandom.current().nextInt(DELIVERY_ITEMS.size()));
        ItemEntity food = new ItemEntity(courier.level(), courier.getX(), courier.getY() + 1.0, courier.getZ(), new ItemStack(item));
        Vec3 throwDirection = player.position().add(0.0, 1.0, 0.0).subtract(food.position()).normalize();
        food.setDeltaMovement(throwDirection.scale(0.35).add(0.0, 0.25, 0.0));
        courier.level().addFreshEntity(food);
    }

    private static void completeTimedOutDelivery(ServerPlayer player, Mob courier, boolean delivered) {
        if (courier.isRemoved()) {
            return;
        }

        if (player.isRemoved()) {
            courier.discard();
            return;
        }

        if (!delivered) {
            Vec3 arrivalPosition = positionBehindPlayer(player, BoiledEggConfig.DELIVERY_HANDOFF_DISTANCE_BLOCKS);
            courier.setPos(arrivalPosition.x, arrivalPosition.y, arrivalPosition.z);
            throwFoodToPlayer(player, courier);
            player.sendSystemMessage(Component.literal("外卖史蒂夫：路太远了，我传送送达！"));
        }

        courier.discard();
    }

    private static void giveFoodDirectly(ServerPlayer player) {
        if (player.isRemoved()) {
            return;
        }

        Item item = DELIVERY_ITEMS.get(ThreadLocalRandom.current().nextInt(DELIVERY_ITEMS.size()));
        player.addItem(new ItemStack(item));
        player.sendSystemMessage(Component.literal("外卖史蒂夫：您的外卖到了！"));
    }

    private record DeliveryStepResult(boolean delivered, Vec3 exitTarget, boolean finished) {
    }
}
