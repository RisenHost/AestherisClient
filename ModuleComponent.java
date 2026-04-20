package com.aetheris.client;

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
//  ModuleComponent.java â€” Aetheris Client  |  Fabric 1.21.1
//
//  Renders a single module entry inside the ClickGUI.
//
//  Mouse controls:
//    Left-click  header â†’ toggle module enabled / disabled
//    Right-click header â†’ expand / collapse settings panel
//
//  Assumptions about your existing code (adjust if your API differs):
//    Module.getName()       â†’ String
//    Module.isEnabled()     â†’ boolean
//    Module.toggle()        â†’ void
//    Module.getSettings()   â†’ List<Setting<?>>
//    NumberSetting          â†’ Setting<Double> with getMin() / getMax()
//    BooleanSetting         â†’ Setting<Boolean>
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class ModuleComponent {

    // â”€â”€ Layout Constants â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private static final int HEADER_H   = 20;   // pixel height of the module header bar
    private static final int SETTING_H  = 20;   // pixel height of each setting row
    private static final int INNER_PAD  = 6;    // horizontal padding inside panels
    private static final int ACCENT_W   = 2;    // width of the left accent bar

    // â”€â”€ Palette â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Header backgrounds
    private static final int HEADER_NORMAL  = 0xFF0E0820;
    private static final int HEADER_HOVER   = 0xFF1C1040;
    private static final int HEADER_ENABLED = 0xFF160C35;

    // Settings panel
    private static final int SETTINGS_BG    = 0xFF090618;
    private static final int SETTINGS_SEP   = 0xFF1E1050; // row separator line

    // Accent bars
    private static final int ACCENT_ON      = 0xFF7C3AED; // vivid royal purple â€” enabled
    private static final int ACCENT_OFF     = 0xFF2D1660; // dim indigo â€” disabled

    // Text
    private static final int TEXT_ENABLED   = 0xFFFFFFFF; // pure white
    private static final int TEXT_DISABLED  = 0xFF7C6FA8; // muted lavender-grey
    private static final int TEXT_ARROW     = 0xFF6D28D9; // expand arrow color

    // Glow (layered semi-transparent fill to fake bloom)
    private static final int GLOW_RGB       = 0x7C3AED;   // purple, alpha added per layer

    // â”€â”€ State â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private int x, y, width;
    private boolean expanded = false;

    /** Smooth hover animation [0.0 â†’ 1.0]. Updated each frame via lerp. */
    private float hoverProgress = 0f;
    private long  lastFrameMs   = System.currentTimeMillis();

    // â”€â”€ References â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    private final Module module;

    /**
     * Setting elements in their original order, interleaved as the Module declares them.
     * Each entry is either a SliderElement or a ToggleElement.
     */
    private final List<Object> settingElements = new ArrayList<>();

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public ModuleComponent(Module module, int x, int y, int width) {
        this.module = module;
        this.x      = x;
        this.y      = y;
        this.width  = width;
        buildElements();
    }

    /**
     * Reads the module's settings list and creates the appropriate UI element
     * for each entry, preserving declaration order.
     *
     * â”€â”€ Adjust class names to match your Setting subclasses â”€â”€
     */
    private void buildElements() {
        settingElements.clear();
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof NumberSetting ns) {
                settingElements.add(new SliderElement(ns, x, 0, width));
            } else if (setting instanceof BooleanSetting bs) {
                settingElements.add(new ToggleElement(bs, x, 0, width));
            }
            // Extend here for future setting types (e.g. EnumSetting, ColorSetting)
        }
    }

    // â”€â”€ Public Render Entry â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public void render(DrawContext context, int mouseX, int mouseY) {
        TextRenderer font = MinecraftClient.getInstance().textRenderer;

        // â”€â”€ Animate hover progress â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        long now  = System.currentTimeMillis();
        float dt  = (now - lastFrameMs) / 1000f;
        lastFrameMs = now;

        boolean headerHovered = isHeaderHovered(mouseX, mouseY);
        float   target        = headerHovered ? 1f : 0f;
        // Ease-in / ease-out lerp â€” snappy approach (10 = speed factor)
        hoverProgress += (target - hoverProgress) * Math.min(1f, dt * 10f);

        renderHeader(context, font, mouseX, mouseY);

        if (expanded && !settingElements.isEmpty()) {
            renderSettingsPanel(context, font, mouseX, mouseY);
        }
    }

    // â”€â”€ Header â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void renderHeader(DrawContext context, TextRenderer font, int mouseX, int mouseY) {
        boolean enabled = module.isEnabled();

        // Background â€” interpolate from normal â†’ hover tint
        int bgColor = lerpColor(
                enabled ? HEADER_ENABLED : HEADER_NORMAL,
                HEADER_HOVER,
                hoverProgress
        );
        context.fill(x, y, x + width, y + HEADER_H, bgColor);

        // Soft purple glow effect radiating outward from the header
        if (hoverProgress > 0.01f) {
            paintGlow(context, x, y, x + width, y + HEADER_H, hoverProgress);
        }

        // Left accent bar â€” vivid when enabled, dark when disabled
        int accentColor = enabled ? ACCENT_ON : ACCENT_OFF;
        context.fill(x, y, x + ACCENT_W, y + HEADER_H, accentColor);

        // Module name
        int nameColor = enabled ? TEXT_ENABLED : TEXT_DISABLED;
        int nameX = x + ACCENT_W + INNER_PAD;
        int nameY = y + (HEADER_H - font.fontHeight) / 2;
        context.drawText(font, module.getName(), nameX, nameY, nameColor, false);

        // Expand/collapse arrow â€” only shown if the module has settings
        if (!settingElements.isEmpty()) {
            String arrow = expanded ? "â–¼" : "â–¶";
            int arrowX = x + width - font.getWidth(arrow) - INNER_PAD;
            context.drawText(font, arrow, arrowX, nameY, TEXT_ARROW, false);
        }
    }

    // â”€â”€ Settings Panel â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private void renderSettingsPanel(DrawContext context, TextRenderer font,
                                     int mouseX, int mouseY) {
        int panelY  = y + HEADER_H;
        int panelH  = settingElements.size() * SETTING_H;
        int panelX2 = x + width;

        // Panel background
        context.fill(x, panelY, panelX2, panelY + panelH, SETTINGS_BG);

        // Thin bottom border
        context.fill(x, panelY + panelH - 1, panelX2, panelY + panelH, ACCENT_OFF);

        // Individual setting rows
        int elemY = panelY;
        for (int i = 0; i < settingElements.size(); i++) {
            Object elem = settingElements.get(i);

            // Row separator (skip first)
            if (i > 0) {
                context.fill(x + INNER_PAD, elemY, panelX2 - INNER_PAD, elemY + 1, SETTINGS_SEP);
            }

            // Position and render the element
            if (elem instanceof SliderElement se) {
                se.setX(x);
                se.setY(elemY);
                se.setWidth(width);
                se.render(context, mouseX, mouseY);
            } else if (elem instanceof ToggleElement te) {
                te.setX(x);
                te.setY(elemY);
                te.setWidth(width);
                te.render(context, mouseX, mouseY);
            }

            elemY += SETTING_H;
        }
    }

    // â”€â”€ Mouse Events â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Returns true if this component consumed the click. */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Header interaction
        if (isHeaderHovered(mouseX, mouseY)) {
            if (button == 0) {           // Left-click â†’ toggle enable
                module.toggle();
                return true;
            } else if (button == 1) {    // Right-click â†’ expand / collapse
                expanded = !expanded;
                return true;
            }
        }

        // Delegate to setting elements when panel is open
        if (expanded) {
            for (Object elem : settingElements) {
                boolean consumed = false;
                if (elem instanceof SliderElement se) consumed = se.mouseClicked(mouseX, mouseY, button);
                else if (elem instanceof ToggleElement te) consumed = te.mouseClicked(mouseX, mouseY, button);
                if (consumed) return true;
            }
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        for (Object elem : settingElements) {
            if (elem instanceof SliderElement se) se.mouseReleased();
        }
    }

    /** Returns true if this component consumed the drag. */
    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (!expanded) return false;
        for (Object elem : settingElements) {
            if (elem instanceof SliderElement se && se.mouseDragged(mouseX, mouseY)) return true;
        }
        return false;
    }

    // â”€â”€ Height Query â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Returns the total rendered height of this component.
     * Used by ClickGuiScreen to stack components without overlap.
     */
    public int getHeight() {
        if (!expanded || settingElements.isEmpty()) return HEADER_H;
        return HEADER_H + settingElements.size() * SETTING_H;
    }

    // â”€â”€ Helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private boolean isHeaderHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + HEADER_H;
    }

    /**
     * Simulates a soft bloom glow by drawing concentric semi-transparent
     * rectangles expanding outward from the target bounds.
     *
     * @param intensity  0.0 (invisible) to 1.0 (full glow)
     */
    private static void paintGlow(DrawContext ctx, int x1, int y1, int x2, int y2, float intensity) {
        // Layer 0: innermost, densest
        // Layer 2: outermost, most transparent
        int[] baseAlphas = { 38, 20, 9 };
        for (int i = 0; i < baseAlphas.length; i++) {
            int layer  = i + 1;
            int alpha  = (int)(baseAlphas[i] * intensity);
            int color  = (alpha << 24) | GLOW_RGB;
            ctx.fill(x1 - layer, y1 - layer, x2 + layer, y2 + layer, color);
        }
    }

    /**
     * Linear interpolation between two ARGB colors.
     *
     * @param t  Blend factor in [0, 1]. 0 = {@code a}, 1 = {@code b}.
     */
    static int lerpColor(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aA = (a >> 24) & 0xFF, rA = (a >> 16) & 0xFF, gA = (a >> 8) & 0xFF, bA = a & 0xFF;
        int aB = (b >> 24) & 0xFF, rB = (b >> 16) & 0xFF, gB = (b >> 8) & 0xFF, bB = b & 0xFF;
        return ((aA + (int)((aB - aA) * t)) << 24)
             | ((rA + (int)((rB - rA) * t)) << 16)
             | ((gA + (int)((gB - gA) * t)) << 8)
             |  (bA + (int)((bB - bA) * t));
    }

    // â”€â”€ Setters (called by ClickGuiScreen each frame) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
          }
