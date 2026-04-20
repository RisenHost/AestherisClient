package com.aetheris.client;

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
//  ToggleElement.java â€” Aetheris Client  |  Fabric 1.21.1
//
//  Renders an animated pill toggle switch for a BooleanSetting.
//
//  Row layout (within a 20px tall setting row):
//
//   â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
//   â”‚  SettingName                    [  â—  ]     â”‚  â† OFF (knob left, dark pill)
//   â”‚  SettingName                    [ â—   ]     â”‚  â† ON  (knob right, purple pill)
//   â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
//
//  The knob slides smoothly from left â†’ right when toggled via a lerp animation.
//
//  Assumptions about your BooleanSetting class (adjust if needed):
//    BooleanSetting extends Setting<Boolean>
//    setting.getName()         â†’ String
//    setting.getValue()        â†’ Boolean
//    setting.setValue(Boolean) â†’ void   (or call setting.toggle() if preferred)
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class ToggleElement {

    // â”€â”€ Layout â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private static final int PAD        = 7;    // horizontal inner padding
    private static final int PILL_W     = 26;   // pill total width
    private static final int PILL_H     = 12;   // pill total height
    private static final int KNOB_SIZE  = 8;    // knob square side length
    private static final int KNOB_PAD   = 2;    // gap between knob edge and pill wall

    // â”€â”€ Palette â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Pill backgrounds
    private static final int PILL_OFF   = 0xFF150D30; // deep void â€” off state
    private static final int PILL_ON    = 0xFF6D28D9; // royal violet â€” on state

    // Knob
    private static final int KNOB_COLOR = 0xFFEDE9FE; // near-white lavender
    private static final int KNOB_GLOW  = 0xA78BFA;   // lavender glow (alpha added dynamically)

    // Text
    private static final int TEXT_NAME  = 0xFFB39DDB; // setting label â€” soft lavender

    // â”€â”€ State â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private int x, y, width;

    /**
     * Animated knob position:
     *   0.0 = fully left  (OFF)
     *   1.0 = fully right (ON)
     * Lerped every frame toward the target value.
     */
    private float knobProgress = 0f;
    private long  lastFrameMs  = System.currentTimeMillis();

    private final BooleanSetting setting; // â† rename to match your class

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * @param setting  The BooleanSetting this toggle controls.
     * @param x        Left edge of the parent settings panel.
     * @param y        Top edge of this element's row.
     * @param width    Full width of the parent settings panel.
     */
    public ToggleElement(BooleanSetting setting, int x, int y, int width) {
        this.setting = setting;
        this.x       = x;
        this.y       = y;
        this.width   = width;
        // Sync animation to actual initial value (no startup flicker)
        this.knobProgress = setting.getValue() ? 1f : 0f;
    }

    // â”€â”€ Render â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public void render(DrawContext context, int mouseX, int mouseY) {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;

        // â”€â”€ Advance knob animation â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        long  now    = System.currentTimeMillis();
        float dt     = (now - lastFrameMs) / 1000f;
        lastFrameMs  = now;

        float target = setting.getValue() ? 1f : 0f;
        // Speed factor 12 â†’ snappy ~80ms transition
        knobProgress += (target - knobProgress) * Math.min(1f, dt * 12f);

        // â”€â”€ Pill geometry â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        int centerY  = y + 10;                       // vertical midpoint of the 20px row
        int pillX1   = x + width - PAD - PILL_W;
        int pillX2   = pillX1 + PILL_W;
        int pillY1   = centerY - PILL_H / 2;
        int pillY2   = pillY1 + PILL_H;

        // â”€â”€ Setting name â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        int nameY = centerY - font.fontHeight / 2;
        context.drawText(font, setting.getName(), x + PAD, nameY, TEXT_NAME, false);

        // â”€â”€ Pill background (color lerps from OFF to ON) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        int pillColor = ModuleComponent.lerpColor(PILL_OFF, PILL_ON, knobProgress);
        context.fill(pillX1, pillY1, pillX2, pillY2, pillColor);

        // â”€â”€ Knob â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        // Travel distance the knob covers inside the pill
        int travel  = PILL_W - KNOB_SIZE - KNOB_PAD * 2;
        int knobX1  = pillX1 + KNOB_PAD + (int)(travel * knobProgress);
        int knobX2  = knobX1 + KNOB_SIZE;
        int knobY1  = centerY - KNOB_SIZE / 2;
        int knobY2  = knobY1 + KNOB_SIZE;

        // Glow aura fades in as knobProgress increases (ON = full glow)
        if (knobProgress > 0.05f) {
            int glowAlpha = (int)(0x60 * knobProgress);
            int glowColor = (glowAlpha << 24) | KNOB_GLOW;
            // Two concentric glow rings
            context.fill(knobX1 - 2, knobY1 - 2, knobX2 + 2, knobY2 + 2, glowColor >> 1);
            context.fill(knobX1 - 1, knobY1 - 1, knobX2 + 1, knobY2 + 1, glowColor);
        }

        // Knob body
        context.fill(knobX1, knobY1, knobX2, knobY2, KNOB_COLOR);
    }

    // â”€â”€ Mouse Events â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Flips the boolean value on left-click within this element's row.
     *
     * @return true if this element consumed the click.
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            // Flip the value â€” replace with setting.toggle() if your API supports it
            setting.setValue(!setting.getValue());
            return true;
        }
        return false;
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** True if (mouseX, mouseY) falls within this element's 20px row. */
    private boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + 20;
    }

    // â”€â”€ Setters â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public void setX(int x)      { this.x     = x; }
    public void setY(int y)      { this.y     = y; }
    public void setWidth(int w)  { this.width = w; }
  }
