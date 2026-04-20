 import com.aetheris.client.module.Category;
import com.aetheris.client.module.Module;
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
    private final NumberSetting fastDelay = new NumberSetting("Fast Delay", 50, 0, 500);
    private final NumberSetting fumbleDelay = new NumberSetting("Fumble Delay", 150, 0, 500);
    private final NumberSetting fumbleChance = new NumberSetting("Fumble Chance", 15, 0, 100);
    private final NumberSetting fastJitter = new NumberSetting("Fast Jitter", 20, 0, 200);
    private final NumberSetting fumbleJitter = new NumberSetting("Fumble Jitter", 40, 0, 200);

    private final NumberSetting delayClampMin = new NumberSetting("Delay Clamp Min", 20, 0, 500);
    private final NumberSetting delayClampMax = new NumberSetting("Delay Clamp Max", 300, 0, 1000);

    private final NumberSetting openDelayMin = new NumberSetting("Open Delay Min", 30, 0, 200);
    private final NumberSetting openDelayMax = new NumberSetting("Open Delay Max", 80, 0, 300);
    private final NumberSetting scanDelayMin = new NumberSetting("Scan Delay Min", 20, 0, 150);
    private final NumberSetting scanDelayMax = new NumberSetting("Scan Delay Max", 60, 0, 200);

    private final BooleanSetting autoOpenInv = new BooleanSetting("Auto Open Inv", true);
    private final BooleanSetting autoClose = new BooleanSetting("Auto Close", true);
    private final BooleanSetting simCursor = new BooleanSetting("Simulate Cursor", true);

    // Runtime state
    private final Random random = new Random();
    private long nextActionTime = 0;
    private boolean isSwapping = false;
    private int targetTotemSlot = -1;

    public AutoTotem() {
        super("AutoTotem", "Automatically replaces popped totems with configurable humanization.", Category.COMBAT, -1);
        
        // REGISTERING EVERY SETTING - This makes them appear in the ClickGUI
        addSetting(fastDelay);
        addSetting(fumbleDelay);
        addSetting(fumbleChance);
        addSetting(fastJitter);
        addSetting(fumbleJitter);
        addSetting(delayClampMin);
        addSetting(delayClampMax);
        addSetting(openDelayMin);
        addSetting(openDelayMax);
        addSetting(scanDelayMin);
        addSetting(scanDelayMax);
        addSetting(autoOpenInv);
        addSetting(autoClose);
        addSetting(simCursor);
    }

    @Override
    protected void onEnable() {
        nextActionTime = System.currentTimeMillis();
        isSwapping = false;
    }

    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        boolean needsTotem = !mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING);
        if (!needsTotem) {
            isSwapping = false;
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextActionTime) return;

        if (mc.currentScreen == null) {
            if (autoOpenInv.getValue()) {
                mc.player.getInventory().open();
                nextActionTime = now + randomDelay(openDelayMin.getValue().intValue(), openDelayMax.getValue().intValue());
                isSwapping = true;
            }
            return;
        }

        if (isSwapping) {
            if (targetTotemSlot == -1) {
                targetTotemSlot = findTotemSlot(mc.player.getInventory());
                nextActionTime = now + randomDelay(scanDelayMin.getValue().intValue(), scanDelayMax.getValue().intValue());
                if (targetTotemSlot == -1) {
                    if (autoClose.getValue()) mc.player.closeHandledScreen();
                    isSwapping = false;
                }
                return;
            }
            performSwap(mc);
        }
    }

    private void performSwap(MinecraftClient mc) {
        if (simCursor.getValue()) {
            try { Thread.sleep(random.nextInt(20, 40)); } catch (InterruptedException ignored) {}
        }

        int syncId = mc.player.currentScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, targetTotemSlot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(syncId, 40, 0, SlotActionType.PICKUP, mc.player);

        if (autoClose.getValue()) mc.player.closeHandledScreen();

        long delay = calculateSwapDelay();
        nextActionTime = System.currentTimeMillis() + delay;
        targetTotemSlot = -1;
        isSwapping = false;
    }

    private long calculateSwapDelay() {
        int baseDelay = fastDelay.getValue().intValue();
        int jitter = fastJitter.getValue().intValue();

        if (random.nextInt(100) < fumbleChance.getValue().intValue()) {
            baseDelay += fumbleDelay.getValue().intValue();
            jitter += fumbleJitter.getValue().intValue();
        }

        long delay = baseDelay + random.nextInt(-jitter, jitter + 1);
        delay = Math.max(delayClampMin.getValue().intValue(), Math.min(delay, delayClampMax.getValue().intValue()));
        return Math.max(0, delay);
    }

    private int findTotemSlot(PlayerInventory inv) {
        for (int i = 9; i <= 44; i++) {
            if (i == 40) continue; 
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                return i;
            }
        }
        return -1;
    }

    private int randomDelay(int min, int max) {
        if (max <= min) return min;
        return random.nextInt(max - min + 1) + min;
    }
}
