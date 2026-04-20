package com.aetheris.client;

// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
//  ClickGuiScreen.java â€” Aetheris Client  |  Fabric 1.21.1
//
//  The root GUI screen. Opens your ClickGUI, renders all ModuleComponents,
//  and wires up mouse events.
//
//  Registration (in your ClientModInitializer or keybind handler):
//      MinecraftClient.getInstance().setScreen(new ClickGuiScreen());
// â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {

    // â”€â”€ Palette â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    /** The "Infinity" void â€” near-black with a ghost of deep violet. */
    private static final int BG_OVERLAY    = 0xD4020009;
    /** Subtle vignette darkening applied at screen edges. */
    private static final int VIGNETTE      = 0x55000000;
    /** Accent title color. */
    private static final int TITLE_PURPLE  = 0xFFA78BFA;
    private static final int TITLE_WHITE   = 0xFFFFFFFF;
    private static final int TITLE_DIM     = 0xFF6D28D9;

    // â”€â”€ Layout â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    /** Fixed X position of the module column. */
    private static final int PANEL_X       = 10;
    /** Width of every ModuleComponent. */
    private static final int PANEL_W       = 175;
    /** Y start â€” leaves room for the header bar. */
    private static final int LIST_Y_START  = 30;
    /** Vertical gap between components. */
    private static final int COMP_GAP      = 2;

    private final List<ModuleComponent> components = new ArrayList<>();

    public ClickGuiScreen() {
        super(Text.literal("Aetheris ClickGUI"));
    }

    // â”€â”€ Lifecycle â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Override
    protected void init() {
        buildComponents();
    }

    /**
     * Rebuilds the component list from ModuleManager.
     * Safe to call on re-open or after adding modules at runtime.
     */
    private void buildComponents() {
        components.clear();
        for (Module module : ModuleManager.getInstance().getModules()) {
            // y=0 is a placeholder; actual Y is computed dynamically each frame
            components.add(new ModuleComponent(module, PANEL_X, 0, PANEL_W));
        }
    }

    // â”€â”€ Rendering â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1. Full-screen "Infinity" void overlay
        context.fill(0, 0, this.width, this.height, BG_OVERLAY);

        // 2. Soft vignette edges for depth (top + bottom gradient)
        context.fillGradient(0, 0,           this.width, 80,          VIGNETTE, 0x00000000);
        context.fillGradient(0, this.height - 80, this.width, this.height, 0x00000000, VIGNETTE);

        // 3. Header bar
        renderHeader(context);

        // 4. Module components â€” stacked vertically, y positions computed live
        int currentY = LIST_Y_START;
        for (ModuleComponent comp : components) {
            comp.setX(PANEL_X);
            comp.setY(currentY);
            comp.render(context, mouseX, mouseY);
            currentY += comp.getHeight() + COMP_GAP;
        }

        // Note: intentionally not calling super.render() â€” no vanilla widgets needed.
    }

    /** Draws the thin decorative header above the module list. */
    private void renderHeader(DrawContext context) {
        var font = this.client.textRenderer;

        // Separator line
        context.fill(PANEL_X, LIST_Y_START - 2, PANEL_X + PANEL_W, LIST_Y_START - 1, TITLE_DIM);

        // "Aetheris" title text
        String title   = "Aetheris";
        String divider = " âœ¦ ";
        String sub     = "ClickGUI";

        int textX = PANEL_X;
        int textY = LIST_Y_START - font.fontHeight - 5;

        context.drawText(font, title,   textX,                                   textY, TITLE_WHITE,  false);
        context.drawText(font, divider, textX + font.getWidth(title),             textY, TITLE_DIM,    false);
        context.drawText(font, sub,     textX + font.getWidth(title + divider),   textY, TITLE_PURPLE, false);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Suppressed â€” we draw our own immersive void in render()
    }

    // â”€â”€ Mouse Events â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (ModuleComponent comp : components) {
            if (comp.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (ModuleComponent comp : components) {
            comp.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (ModuleComponent comp : components) {
            if (comp.mouseDragged(mouseX, mouseY, button)) return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    // â”€â”€ Screen Config â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Keep the game world running while the GUI is open (client-side mod). */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Press Escape to close. */
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
  }
