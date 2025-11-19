package com.zivalez.latencyhud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class LatencyHudOverlay implements LayeredDraw.Layer {
    public static final LatencyHudOverlay INSTANCE = new LatencyHudOverlay();
    private final LatencyTracker tracker = new LatencyTracker();
    private float displayedMs = 0;

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!Config.ENABLED.get()) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.getDebugOverlay().showDebugScreen()) return;

        // Update tracker
        float frameTimeMs = mc.getFrameTimeNs() / 1_000_000f;
        tracker.addSample(frameTimeMs);

        // Interpolate displayed value for smooth text
        displayedMs = Mth.lerp(0.1f, displayedMs, frameTimeMs);

        renderHud(guiGraphics, mc);
    }

    private void renderHud(GuiGraphics guiGraphics, Minecraft mc) {
        int x = Config.HUD_X.get();
        int y = Config.HUD_Y.get();
        int width = 120;
        int height = 40;
        int graphHeight = 25;

        // Draw background
        guiGraphics.fill(x, y, x + width, y + height, 0x80000000);

        // Draw Text
        double avg = tracker.getAverage();
        int color = getColorForLatency((float) displayedMs);
        String text = String.format("%.1f ms (Avg: %.1f)", displayedMs, avg);
        guiGraphics.drawString(mc.font, text, x + 4, y + 4, color);

        // Alert
        if (displayedMs > avg * Config.THRESHOLD_MULTIPLIER.get()) {
            guiGraphics.drawString(mc.font, "!", x + width - 10, y + 4, 0xFFFF0000);
        }

        // Draw Graph
        renderGraph(guiGraphics, x, y + height - graphHeight, width, graphHeight);
    }

    private void renderGraph(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        float maxMs = Math.max(50f, tracker.getMax()); // Minimum 50ms scale
        int samples = tracker.getCount();
        if (samples < 2) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        
        Tesselator tesselator = Tesselator.getInstance();
        // In 1.21, use begin() then end() which draws.
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(x, y + height, 0);

        float stepX = (float) width / (samples - 1);

        for (int i = 0; i < samples; i++) {
            float val = tracker.getSample(samples - 1 - i);
            float h = (val / maxMs) * height;
            h = Math.min(h, height);

            float px = i * stepX;
            int col = getColorForLatency(val);
            int r = (col >> 16) & 0xFF;
            int g = (col >> 8) & 0xFF;
            int b = col & 0xFF;
            int a = (col >> 24) & 0xFF;
            
            // Top vertex (colored)
            buffer.addVertex(pose.last().pose(), px, -h, 0).setColor(r, g, b, a);
            // Bottom vertex (transparent/faded)
            buffer.addVertex(pose.last().pose(), px, 0, 0).setColor(r, g, b, 50);
        }

        // Draw
        try {
            // BufferBuilder.end() returns MeshData, which we then draw via BufferUploader
            // But Tesselator.end() handles this convenience in most versions.
            // If Tesselator.end() is void, it draws.
            net.minecraft.client.renderer.BufferUploader.drawWithShader(buffer.buildOrThrow());
        } catch (Exception e) {
            // Fallback if API differs slightly, but buildOrThrow is standard in 1.21
        }

        pose.popPose();
        RenderSystem.disableBlend();
    }

    private int getColorForLatency(float ms) {
        // Green (0ms) -> Yellow (30ms) -> Red (60ms+)
        // HSB: Hue 0.33 (Green) -> 0.0 (Red)
        float hue = (1.0f - Mth.clamp(ms / 60.0f, 0f, 1f)) * 0.33f;
        return Mth.hsvToRgb(hue, 1.0f, 1.0f) | 0xFF000000;
    }
}
