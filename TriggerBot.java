package com.aetheris.client.module.impl.combat;

import com.aetheris.client.module.Category;
import com.aetheris.client.module.Module;
import com.aetheris.client.setting.BooleanSetting;
import com.aetheris.client.setting.NumberSetting;
import com.aetheris.client.util.PlayerUtils; // hypothetical utility
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.Random;

/**
 * Automatically attacks the entity under the crosshair with fully configurable delays,
 * cooldowns, and target filters.
 */
public class TriggerBot extends Module {

    // ---------- Target Filters ----------
    private final BooleanSetting playersOnly = new BooleanSetting("Players Only", "Only attack players", true);
    private final BooleanSetting ignoreFriends = new BooleanSetting("Ignore Friends", "Do not attack friends", true);
    private final BooleanSetting onShield = new BooleanSetting("On Shield", "Attack even if target is blocking", false);

    // ---------- Attack Cooldown Settings ----------
    private final NumberSetting minCooldown = new NumberSetting("Min Cooldown", "Minimum cooldown between attacks (ms)", 400, 0, 2000, 10);
    private final NumberSetting maxCooldown = new NumberSetting("Max Cooldown", "Maximum cooldown between attacks (ms)", 600, 0, 2000, 10);

    // ---------- Delay Before Attacking (Reaction Time) ----------
    private final NumberSetting minDelay = new NumberSetting("Min Delay", "Minimum reaction delay after target acquired (ms)", 50, 0, 500, 5);
    private final NumberSetting maxDelay = new NumberSetting("Max Delay", "Maximum reaction delay after target acquired (ms)", 150, 0, 500, 5);

    // ---------- Swing Range Jitter ----------
    private final NumberSetting minSwingRange = new NumberSetting("Min Swing Range", "Minimum attack reach", 2.8, 1.0, 6.0, 0.1);
    private final NumberSetting maxSwingRange = new NumberSetting("Max Swing Range", "Maximum attack reach", 3.2, 1.0, 6.0, 0.1);

    // Runtime state
    private final Random random = new Random();
    private long lastAttackTime = 0;
    private long targetAcquiredTime = 0;
    private Entity currentTarget = null;

    public TriggerBot() {
        super("TriggerBot", "Automatically attacks entities under crosshair with bypass customization.",
                Category.COMBAT, -1);
    }

    @Override
    protected void onEnable() {
        lastAttackTime = 0;
        targetAcquiredTime = 0;
        currentTarget = null;
    }

    @Override
    protected void onDisable() {
        // No cleanup needed
    }

    /**
     * Called every client tick (e.g., from ClientTickEvents.END_TICK).
     */
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();

        // Check attack cooldown (vanilla cooldown is also respected automatically)
        if (now - lastAttackTime < getCurrentCooldown()) {
            return;
        }

        // Get entity under crosshair
        Entity target = getTargetEntity(mc);
        if (target == null) {
            currentTarget = null;
            targetAcquiredTime = 0;
            return;
        }

        // Apply filters
        if (!isValidTarget(target)) {
            currentTarget = null;
            targetAcquiredTime = 0;
            return;
        }

        // New target acquired: start reaction timer
        if (currentTarget != target) {
            currentTarget = target;
            targetAcquiredTime = now;
            return;
        }

        // Wait for reaction delay
        long reactionDelay = randomDelay(minDelay.getValue().intValue(), maxDelay.getValue().intValue());
        if (now - targetAcquiredTime < reactionDelay) {
            return;
        }

        // Check range (with jitter)
        double distance = mc.player.distanceTo(target);
        double requiredRange = getCurrentRange();
        if (distance > requiredRange) {
            return;
        }

        // Attack!
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttackTime = now;
        // Reset target to allow re-acquisition after cooldown
        currentTarget = null;
        targetAcquiredTime = 0;
    }

    private Entity getTargetEntity(MinecraftClient mc) {
        HitResult hit = mc.crosshairTarget;
        if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult) hit).getEntity();
        }
        return null;
    }

    private boolean isValidTarget(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
            return false;
        }

        // Players only filter
        if (playersOnly.getValue() && !(entity instanceof PlayerEntity)) {
            return false;
        }

        // Friend system integration (hypothetical)
        if (ignoreFriends.getValue() && isFriend(entity)) {
            return false;
        }

        // Shield check
        if (entity instanceof PlayerEntity player && player.isBlocking()) {
            return onShield.getValue();
        }

        return true;
    }

    private boolean isFriend(Entity entity) {
        // Placeholder: integrate with your Friends module/manager
        return false;
    }

    private long getCurrentCooldown() {
        int min = minCooldown.getValue().intValue();
        int max = maxCooldown.getValue().intValue();
        return randomDelay(min, max);
    }

    private double getCurrentRange() {
        double min = minSwingRange.getValue();
        double max = maxSwingRange.getValue();
        return min + random.nextDouble() * (max - min);
    }

    private int randomDelay(int min, int max) {
        if (max <= min) return min;
        return random.nextInt(max - min + 1) + min;
    }
          }
