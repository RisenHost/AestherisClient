package com.aetheris.client.render.hud;

import com.aetheris.client.module.Module;
import com.aetheris.client.module.ModuleManager;
import com.aetheris.client.util.ColorUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HudRenderer.java â€” Aetheris Client
 *
 * Renders two HUD elements using Fabric's HudRenderCallback:
 *
 *   1. Watermark (top-left)
 *      â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
 *      â–Œ Aetheris  |  1.21.1     â”‚
 *      â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
 *
 *   2. ArrayList (top-right)
 *      â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”
 *      â”‚  KillAura      â–Œ â”‚  â† lavender
 *      â”‚  Scaffold      â–Œ â”‚
 *      â”‚  Speed         â–Œ â”‚  â† deep purple
 *      â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜
 *
 * Registration:
 *   Call HudRenderer.register() once inside your ClientModInitializer.
 *   e.g. in AetherisClient.java:
 *
 *       @Override
 *       public void onInitializeClient() {
 *           ModuleManager.getInstance().init();
 *           HudRenderer.register();
 *       }
 */
public class HudRenderer {

    // â”€â”€â”€ Layout Constants â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Horizontal/vertical padding inside every panel. */
    private static final int PAD = 4;

    /** Height (in px) of each ArrayList row: font height + row gap. */
    private static final int ROW_GAP = 2;

    /** Width of the vertical accent bar drawn on the left (watermark) or right (modules). */
    private static final int ACCENT_W = 2;

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Registers the HUD renderer with Fabric's event bus.
     * Call this once during client initialization.
     */
    public static void register() {
        HudRenderCallback.EVENT.register(HudRenderer::onHudRender);
    }

    /**
     * Main render callback, fired every frame after the vanilla HUD is drawn.
     *
     * @param context     The draw context for this frame.
     * @param tickCounter Provides partial ticks for smooth animations.
     */
    private static void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // Safety guard â€” don't render on title screen or loading screens
        if (mc.player == null || mc.world == null) return;

        renderWatermark(context, mc);
        renderArrayList(context, mc);
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    //  1. Watermark
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Draws the client watermark in the top-left corner.
     *
     * Layout (left â†’ right):
     *   [accent bar] [PAD] "Aetheris" [PAD] "|" [PAD] "1.21.1" [PAD]
     *
     * The "Aetheris" text is pure white; the separator and version are lavender.
     */
    private static void renderWatermark(DrawContext context, MinecraftClient mc) {
        TextRenderer font = mc.textRenderer;

        final String name      = "Aetheris";
        final String separator = " | ";
        final String version   = "1.21.1";
        final String full      = name + separator + version;

        int textW = font.getWidth(full);
        int textH = font.fontHeight;

        // Panel bounds
        int x1 = PAD;
        int y1 = PAD;
        int x2 = x1 + ACCENT_W + PAD + textW + PAD;
        int y2 = y1 + PAD + textH + PAD;

        // â”€â”€ Background panel â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        context.fill(x1, y1, x2, y2, ColorUtils.BACKGROUND);

        // â”€â”€ Left accent bar â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        context.fill(x1, y1, x1 + ACCENT_W, y2, ColorUtils.PURPLE_VIVID);

        // â”€â”€ Text â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        int textX = x1 + ACCENT_W + PAD;
        int textY = y1 + PAD;

        // "Aetheris" in white
        context.drawText(font, name, textX, textY, ColorUtils.WHITE, false);

        // " | 1.21.1" in lavender
        int afterName = textX + font.getWidth(name);
        context.drawText(font, separator + version, afterName, textY, ColorUtils.PURPLE_LIGHT, false);
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    //  2. ArrayList
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Draws the list of enabled modules in the top-right corner.
     *
     * Modules are sorted by display-name width (widest first) so the panel
     * forms a clean staircase rather than a ragged edge.
     *
     * Each row has:
     *   [text] [PAD] [accent bar] flush against the right screen edge
     *
     * The accent bar and text color shift from PURPLE_LIGHT (top) â†’ PURPLE_DEEP
     * (bottom) using linear interpolation across the full list.
     */
    private static void renderArrayList(DrawContext context, MinecraftClient mc) {
        TextRenderer font  = mc.textRenderer;
        int screenW        = mc.getWindow().getScaledWidth();

        // Collect enabled modules, longest name first
        List<Module> active = ModuleManager.getInstance()
                .getModules()
                .stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt((Module m) ->
                        font.getWidth(m.getName())).reversed())
                .collect(Collectors.toList());

        if (active.isEmpty()) return;

        int rowH = font.fontHeight + ROW_GAP;
        int total = active.size();

        for (int i = 0; i < total; i++) {
            Module module = active.get(i);
            String name   = module.getName();
            int textW     = font.getWidth(name);

            // Gradient progress: 0.0 at top â†’ 1.0 at bottom
            float t = (total == 1) ? 0f : (float) i / (total - 1);
            int gradColor = ColorUtils.interpolate(ColorUtils.PURPLE_LIGHT, ColorUtils.PURPLE_DEEP, t);

            // Row vertical position
            int rowY = PAD + i * rowH;

            // Panel bounds (flush right, width fits text + padding + accent bar)
            int panelX1 = screenW - PAD - textW - PAD - ACCENT_W;
            int panelX2 = screenW;
            int panelY1 = rowY - 1;
            int panelY2 = rowY + font.fontHeight + 1;

            // â”€â”€ Background â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            context.fill(panelX1, panelY1, panelX2, panelY2, ColorUtils.BACKGROUND);

            // â”€â”€ Right accent bar â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            context.fill(screenW - ACCENT_W, panelY1, screenW, panelY2, gradColor);

            // â”€â”€ Module name â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
            int textX = panelX1 + PAD;
            context.drawText(font, name, textX, rowY, gradColor, false);
        }
    }
  }
