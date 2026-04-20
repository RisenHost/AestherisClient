package com.aetheris.client;

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
//  SliderElement.java â€” Aetheris Client  |  Fabric 1.21.1
//
//  Renders a horizontal drag-slider for a NumberSetting inside the ClickGUI.
//
//  Row layout (within a 20px tall setting row):
//
//   â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
//   â”‚  SettingName                     1.50   â”‚  â† name + value (top ~10px)
//   â”‚  â–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–ˆâ–‘â–‘â–‘â–‘â–‘â–‘â–‘â–‘â–‘â–‘â–‘â–‘â–‘â–‘â–‘â–‘ [â—]    â”‚  â† track + fill + thumb (bottom)
//   â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
//
//  Assumptions about your NumberSetting class (adjust if needed):
//    NumberSetting extends Setting<Double>
//    setting.getName()   â†’ String
//    setting.getValue()  â†’ Double
//    setting.getMin()    â†’ double
//    setting.getMax()    â†’ double
//    setting.setValue(Double newValue)
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public class SliderElement {

    // â”€â”€ Layout â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private static final int PAD        = 7;   // horizontal padding inside the row
    private static final int TRACK_Y    = 14;  // y offset of track from row top
    private static final int TRACK_H    = 3;   // track height in pixels
    private static final int THUMB_W    = 5;   // thumb rectangle width
    private static final int THUMB_OVER = 2;   // thumb extends this many px above/below track

    // â”€â”€ Palette â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private static final int COLOR_TRACK_BG   = 0xFF140D2A; // empty track â€” deep void
    private static final int COLOR_FILL       = 0xFF6D28D9; // filled portion â€” royal violet
    private static final int COLOR_FILL_GLOW  = 0x306D28D9; // glow behind fill
    private static final int COLOR_THUMB      = 0xFFA78BFA; // thumb knob â€” lavender
    private static final int COLOR_THUMB_GLOW = 0x50A78BFA; // soft aura around thumb
    private static final int COLOR_NAME       = 0xFFB39DDB; // setting label â€” soft lavender
    private static final int COLOR_VALUE      = 0xFFFFFFFF; // current value â€” pure white

    // â”€â”€ State â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private int x, y, width;
    private boolean dragging = false;

    private final NumberSetting setting; // â† rename to match your class

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * @param setting  The NumberSetting this slider controls.
     * @param x        Left edge of the parent settings panel.
     * @param y        Top edge of this element's row.
     * @param width    Full width of the parent settings panel.
     */
    public SliderElement(NumberSetting setting, int x, int y, int width) {
        this.setting = setting;
        this.x       = x;
        this.y       = y;
        this.width   = width;
    }

    // â”€â”€ Render â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public void render(DrawContext context, int mouseX, int mouseY) {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;

        double value = setting.getValue();
        double min   = setting.getMin();
        double max   = setting.getMax();
        double ratio = (max == min) ? 0.0 : Math.max(0.0, Math.min(1.0, (value - min) / (max - min)));

        int trackX1  = x + PAD;
        int trackX2  = x + width - PAD;
        int trackLen = trackX2 - trackX1;
        int trackY   = y + TRACK_Y;

        // â”€â”€ Setting name (top-left of row) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        context.drawText(font, setting.getName(), x + PAD, y + 3, COLOR_NAME, false);

        // â”€â”€ Current value (top-right of row) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        String valStr = formatValue(value);
        int valX = x + width - font.getWidth(valStr) - PAD;
        context.drawText(font, valStr, valX, y + 3, COLOR_VALUE, false);

        // â”€â”€ Empty track â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        context.fill(trackX1, trackY, trackX2, trackY + TRACK_H, COLOR_TRACK_BG);

        // â”€â”€ Filled portion with subtle under-glow â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        int fillX2 = trackX1 + (int)(trackLen * ratio);
        if (fillX2 > trackX1) {
            context.fill(trackX1, trackY + 1, fillX2, trackY + TRACK_H + 1, COLOR_FILL_GLOW);
            context.fill(trackX1, trackY,     fillX2, trackY + TRACK_H,     COLOR_FILL);
        }

        // â”€â”€ Thumb â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        int thumbCenterX = trackX1 + (int)(trackLen * ratio);
        int thumbX1 = thumbCenterX - THUMB_W / 2;
        int thumbX2 = thumbX1 + THUMB_W;
        int thumbY1 = trackY - THUMB_OVER;
        int thumbY2 = trackY + TRACK_H + THUMB_OVER;

        // Glow ring around thumb
        context.fill(thumbX1 - 1, thumbY1 - 1, thumbX2 + 1, thumbY2 + 1, COLOR_THUMB_GLOW);
        // Thumb body
        context.fill(thumbX1, thumbY1, thumbX2, thumbY2, COLOR_THUMB);
    }

    // â”€â”€ Mouse Events â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Starts dragging if the user clicks within this element's row.
     *
     * @return true if this element consumed the click.
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            dragging = true;
            applyDrag(mouseX);
            return true;
        }
        return false;
    }

    /**
     * Continues updating the value while the mouse is held and dragged.
     *
     * @return true if this element is currently being dragged.
     */
    public boolean mouseDragged(double mouseX, double mouseY) {
        if (dragging) {
            applyDrag(mouseX);
            return true;
        }
        return false;
    }

    /** Ends a drag gesture. Call on mouseReleased from the parent. */
    public void mouseReleased() {
        dragging = false;
    }

    // â”€â”€ Internals â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Computes the new setting value from the current mouse X and applies it. */
    private void applyDrag(double mouseX) {
        int trackX1 = x + PAD;
        int trackX2 = x + width - PAD;

        double ratio   = (mouseX - trackX1) / (double)(trackX2 - trackX1);
        ratio          = Math.max(0.0, Math.min(1.0, ratio));
        double rawVal  = setting.getMin() + ratio * (setting.getMax() - setting.getMin());

        // Round to 2 decimal places for clean values
        double rounded = Math.round(rawVal * 100.0) / 100.0;
        setting.setValue(rounded);
    }

    /** True if (mouseX, mouseY) falls within this element's 20px row. */
    private boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + 20;
    }

    /**
     * Formats a double for display.
     * Whole numbers â†’ "42", decimals â†’ "3.14"
     */
    private static String formatValue(double v) {
        if (v == Math.floor(v) && !Double.isInfinite(v)) {
            return String.valueOf((int) v);
        }
        return String.format("%.2f", v);
    }

    // â”€â”€ Setters â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public void setX(int x)       { this.x     = x;     }
    public void setY(int y)       { this.y     = y;     }
    public void setWidth(int w)   { this.width = w;     }
}
