package com.aetheris.client.module.impl.combat;

import com.aetheris.client.module.Category;
import com.aetheris.client.module.Module;
import com.aetheris.client.setting.BooleanSetting;
import com.aetheris.client.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

import java.util.Random;

/**
 * Automatically replaces the offhand totem with a new one when popped.
 * All timings and humanization parameters are fully configurable.
 */
public class AutoTotem extends Module {

    // ---------- Settings ----------
    // Delay and jitter for normal (fast) replacement
    private final NumberSetting fastDelay = new NumberSetting("Fast Delay", "Base delay for quick totem swap (ms)", 50, 0, 500, 1);
    private final NumberSetting fumbleDelay = new NumberSetting("Fumble Delay", "Extra delay added when 'fumbling' occurs (ms)", 150, 0, 500, 1);
    private final NumberSetting fumbleChance = new NumberSetting("Fumble Chance", "Probability of a fumble per swap (%)", 15, 0, 100, 1);
    private final NumberSetting fastJitter = new NumberSetting("Fast Jitter", "Random jitter added to fast delay (ms)", 20, 0, 200, 1);
    private final NumberSetting fumbleJitter = new NumberSetting("Fumble Jitter", "Random jitter added to fumble delay (ms)", 40, 0, 200, 1);

    // Delay clamping (prevents unrealistic timings)
    private final NumberSetting delayClampMin = new NumberSetting("Delay Clamp Min", "Minimum allowed delay after calculation (ms)", 20, 0, 500, 1);
    private final NumberSetting delayClampMax = new NumberSetting("Delay Clamp Max", "Maximum allowed delay after calculation (ms)", 300, 0, 1000, 1);

    // Inventory action delays
    private final NumberSetting openDelayMin = new NumberSetting("Open Delay Min", "Min delay after opening inventory (ms)", 30, 0, 200, 1);
    private final NumberSetting openDelayMax = new NumberSetting("Open Delay Max", "Max delay after opening inventory (ms)", 80, 0, 300, 1);
    private final NumberSetting scanDelayMin = new NumberSetting("Scan Delay Min", "Min delay to locate totem (ms)", 20, 0, 150, 1);
    private final NumberSetting scanDelayMax = new NumberSetting("Scan Delay Max", "Max delay to locate totem (ms)", 60, 0, 200, 1);

    // Behavioral toggles
    private final BooleanSetting autoOpenInv = new BooleanSetting("Auto Open Inv", "Automatically open inventory if closed", true);
    private final BooleanSetting autoClose = new BooleanSetting("Auto Close", "Close inventory after swap", true);
    private final BooleanSetting simCursor = new BooleanSetting("Simulate Cursor", "Simulate cursor movement before clicking", true);

    // Runtime state
    private final Random random = new Random();
    private long nextActionTime = 0;
    private boolean isSwapping = false;
    private int targetTotemSlot = -1;

    public AutoTotem() {
        super("AutoTotem", "Automatically replaces popped totems with configurable humanization.",
                Category.COMBAT, -1); // Keybind set via GUI
    }

    @Override
    protected void onEnable() {
        nextActionTime = System.currentTimeMillis();
        isSwapping = false;
    }

    @Override
    protected void onDisable() {
        // Cleanup not required
    }

    /**
     * Called every client tick (e.g., from ClientTickEvents.END_TICK).
     * Handles totem detection and timed inventory actions.
     */
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // Only act if offhand totem is missing (popped) and we're not already swapping
        boolean needsTotem = !mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
        if (!needsTotem) {
            isSwapping = false;
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextActionTime) return;

        // If inventory is closed and we need to open it
        if (mc.currentScreen == null) {
            if (autoOpenInv.getValue()) {
                // Simulate opening inventory
                mc.player.getInventory().open();
                // Add humanized open delay
                nextActionTime = now + randomDelay(openDelayMin.getValue().intValue(), openDelayMax.getValue().intValue());
                isSwapping = true;
            }
            return;
        }

        // Inventory is open
        if (isSwapping) {
            // Locate a totem in inventory
            if (targetTotemSlot == -1) {
                targetTotemSlot = findTotemSlot(mc.player.getInventory());
                // Add scan delay to mimic searching
                nextActionTime = now + randomDelay(scanDelayMin.getValue().intValue(), scanDelayMax.getValue().intValue());
                if (targetTotemSlot == -1) {
                    // No totem found, abort swap
                    if (autoClose.getValue()) mc.player.closeHandledScreen();
                    isSwapping = false;
                }
                return;
            }

            // Perform the swap
            performSwap(mc);
        }
    }

    private void performSwap(MinecraftClient mc) {
        // Simulate cursor movement if enabled
        if (simCursor.getValue()) {
            // In a real implementation, you'd move the mouse gradually to the slot.
            // Placeholder: add a small delay to fake cursor travel.
            try {
                Thread.sleep(random.nextInt(20, 40));
            } catch (InterruptedException ignored) {}
        }

        // Click the totem slot (slot index conversion: hotbar 36-44, main 9-35)
        int syncId = mc.player.currentScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, targetTotemSlot, 0, SlotActionType.PICKUP, mc.player);
        // Move it to offhand (slot 40)
        mc.interactionManager.clickSlot(syncId, 40, 0, SlotActionType.PICKUP, mc.player);

        // Close inventory if configured
        if (autoClose.getValue()) {
            mc.player.closeHandledScreen();
        }

        // Calculate cooldown for next possible swap (with fumble chance and jitter)
        long delay = calculateSwapDelay();
        nextActionTime = System.currentTimeMillis() + delay;

        // Reset state
        targetTotemSlot = -1;
        isSwapping = false;
    }

    private long calculateSwapDelay() {
        int baseDelay = fastDelay.getValue().intValue();
        int jitter = fastJitter.getValue().intValue();

        // Apply fumble if random chance triggers
        if (random.nextInt(100) < fumbleChance.getValue().intValue()) {
            baseDelay += fumbleDelay.getValue().intValue();
            jitter += fumbleJitter.getValue().intValue();
        }

        long delay = baseDelay + random.nextInt(-jitter, jitter + 1);
        // Clamp to configured bounds
        delay = Math.max(delayClampMin.getValue().intValue(), Math.min(delay, delayClampMax.getValue().intValue()));
        return Math.max(0, delay);
    }

    private int findTotemSlot(PlayerInventory inv) {
        // Search main inventory (indices 9 to 35) first, then hotbar (36-44)
        for (int i = 9; i <= 44; i++) {
            if (i == 40) continue; // skip offhand
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                return i;
            }
        }
        return -1;
    }

    private int randomDelay(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
  }
