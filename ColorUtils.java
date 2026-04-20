package com.aetheris.client.util;

/**
 * ColorUtils.java â€” Aetheris Client
 *
 * Gojo Satoru color palette:
 *   Deep purples, icy whites, and translucent darks.
 *   All colors are in ARGB format (0xAARRGGBB).
 *
 * Usage:
 *   int color = ColorUtils.PURPLE_VIVID;
 *   int blended = ColorUtils.interpolate(ColorUtils.PURPLE_LIGHT, ColorUtils.PURPLE_DEEP, 0.5f);
 *   int chroma  = ColorUtils.getChromaColor(index * 150L); // animated rainbow shifted toward purple
 */
public class ColorUtils {

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    //  Core Palette â€” Gojo "Six Eyes" Theme
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Icy white â€” text, highlights */
    public static final int WHITE          = 0xFFFFFFFF;

    /** Soft off-white â€” secondary text */
    public static final int WHITE_SOFT     = 0xFFE8E0F5;

    /** Lavender â€” top of gradient, light module names */
    public static final int PURPLE_LIGHT   = 0xFFCFB3F5;

    /** Vivid electric purple â€” accent bars, watermark separator */
    public static final int PURPLE_VIVID   = 0xFFAB47BC;

    /** Rich purple â€” mid gradient */
    public static final int PURPLE_MID     = 0xFF8E24AA;

    /** Deep imperial purple â€” bottom of gradient */
    public static final int PURPLE_DEEP    = 0xFF6A0080;

    /** Near-black purple â€” watermark/module backgrounds */
    public static final int BACKGROUND     = 0xB3100820;

    /** Slightly lighter background for subtle hover/divider effects */
    public static final int BACKGROUND_ALT = 0x991A0D35;

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    //  Utility Methods
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Linearly interpolates between two ARGB colors.
     *
     * @param colorA  Starting color (ARGB)
     * @param colorB  Ending color (ARGB)
     * @param t       Blend factor in [0.0, 1.0]. 0 = colorA, 1 = colorB.
     * @return        The interpolated ARGB color.
     */
    public static int interpolate(int colorA, int colorB, float t) {
        t = Math.max(0f, Math.min(1f, t)); // clamp

        int a1 = (colorA >> 24) & 0xFF;
        int r1 = (colorA >> 16) & 0xFF;
        int g1 = (colorA >>  8) & 0xFF;
        int b1 =  colorA        & 0xFF;

        int a2 = (colorB >> 24) & 0xFF;
        int r2 = (colorB >> 16) & 0xFF;
        int g2 = (colorB >>  8) & 0xFF;
        int b2 =  colorB        & 0xFF;

        int a = a1 + (int)((a2 - a1) * t);
        int r = r1 + (int)((r2 - r1) * t);
        int g = g1 + (int)((g2 - g1) * t);
        int b = b1 + (int)((b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Returns an animated chroma color biased toward the purple spectrum.
     * The hue cycles through the purple/blue/pink range over ~3 seconds.
     *
     * @param timeOffset  Offset in milliseconds (use index * 150L for staggered entries).
     * @return            An ARGB color fully opaque.
     */
    public static int getChromaColor(long timeOffset) {
        // Oscillate hue between 0.70 (blue-violet) and 0.85 (magenta-pink)
        long time   = (System.currentTimeMillis() + timeOffset) % 3000;
        float phase = time / 3000f; // 0.0 â†’ 1.0
        // Sine wave to smoothly bounce the hue
        float hue   = 0.75f + 0.07f * (float) Math.sin(phase * 2.0 * Math.PI);

        int rgb = java.awt.Color.HSBtoRGB(hue, 0.75f, 1.0f);
        // Force full alpha
        return (0xFF << 24) | (rgb & 0x00FFFFFF);
    }

    /**
     * Converts a standard packed RGB int (no alpha) to ARGB with the given alpha.
     *
     * @param rgb    Packed RGB (0x00RRGGBB)
     * @param alpha  Alpha value [0â€“255]
     * @return       Packed ARGB (0xAARRGGBB)
     */
    public static int withAlpha(int rgb, int alpha) {
        return ((alpha & 0xFF) << 24) | (rgb & 0x00FFFFFF);
    }
}
