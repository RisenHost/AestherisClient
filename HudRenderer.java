package com.aetheris.client.render.hud;

import com.aetheris.client.module.Module;
import com.aetheris.client.module.ModuleManager;
import com.aetheris.client.util.ColorUtils;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HudRenderer {

    private static final int PAD = 4;
    private static final int ROW_GAP = 2;
    private static final int ACCENT_W = 2;

    public static void register() {
        HudRenderCallback.EVENT.register(HudRenderer::onHudRender);
    }

    private static void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // â”€â”€â”€â”€ THE LIGHT SWITCH: Opens the ClickGUI on Right Shift â”€â”€â”€â”€
        if (mc.currentScreen == null && net.minecraft.client.util.InputUtil.isKeyPressed(
                mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            mc.setScreen(new com.aetheris.client.gui.ClickGuiScreen());
        }

        renderWatermark(context, mc);
        renderArrayList(context, mc);
    }

    private static void renderWatermark(DrawContext context, MinecraftClient mc) {
        TextRenderer font = mc.textRenderer;
        final String name = "Aetheris", separator = " | ", version = "1.21.1";
        final String full = name + separator + version;

        int textW = font.getWidth(full), textH = font.fontHeight;
        int x1 = PAD, y1 = PAD, x2 = x1 + ACCENT_W + PAD + textW + PAD, y2 = y1 + PAD + textH + PAD;

        context.fill(x1, y1, x2, y2, ColorUtils.BACKGROUND);
        context.fill(x1, y1, x1 + ACCENT_W, y2, ColorUtils.PURPLE_VIVID);

        int textX = x1 + ACCENT_W + PAD, textY = y1 + PAD;
        context.drawText(font, name, textX, textY, ColorUtils.WHITE, false);
        context.drawText(font, separator + version, textX + font.getWidth(name), textY, ColorUtils.PURPLE_LIGHT, false);
    }

    private static void renderArrayList(DrawContext context, MinecraftClient mc) {
        TextRenderer font = mc.textRenderer;
        int screenW = mc.getWindow().getScaledWidth();

        List<Module> active = ModuleManager.getInstance().getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt((Module m) -> font.getWidth(m.getName())).reversed())
                .collect(Collectors.toList());

        if (active.isEmpty()) return;

        int rowH = font.fontHeight + ROW_GAP;
        for (int i = 0; i < active.size(); i++) {
            Module module = active.get(i);
            int textW = font.getWidth(module.getName());
            float t = (active.size() == 1) ? 0f : (float) i / (active.size() - 1);
            int gradColor = ColorUtils.interpolate(ColorUtils.PURPLE_LIGHT, ColorUtils.PURPLE_DEEP, t);

            int rowY = PAD + i * rowH;
            int pX1 = screenW - PAD - textW - PAD - ACCENT_W, pY1 = rowY - 1, pY2 = rowY + font.fontHeight + 1;

            context.fill(pX1, pY1, screenW, pY2, ColorUtils.BACKGROUND);
            context.fill(screenW - ACCENT_W, pY1, screenW, pY2, gradColor);
            context.drawText(font, module.getName(), pX1 + PAD, rowY, gradColor, false);
        }
    }
}
