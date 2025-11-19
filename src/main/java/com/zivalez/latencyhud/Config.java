package com.zivalez.latencyhud;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue HUD_X;
    public static final ModConfigSpec.IntValue HUD_Y;
    public static final ModConfigSpec.IntValue HISTORY_LENGTH;
    public static final ModConfigSpec.IntValue COLOR_LOW;
    public static final ModConfigSpec.IntValue COLOR_HIGH;
    public static final ModConfigSpec.DoubleValue THRESHOLD_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue ENABLED;

    static {
        BUILDER.push("general");

        ENABLED = BUILDER
                .comment("Enable or disable the Latency HUD")
                .define("enabled", true);

        HISTORY_LENGTH = BUILDER
                .comment("Number of frames to keep in history for the graph")
                .defineInRange("historyLength", 600, 60, 2400);

        THRESHOLD_MULTIPLIER = BUILDER
                .comment("Multiplier for average frame time to trigger high latency alert (e.g. 1.5 means 50% higher than average)")
                .defineInRange("thresholdMultiplier", 1.5, 1.0, 10.0);

        BUILDER.pop();

        BUILDER.push("visuals");

        HUD_X = BUILDER
                .comment("X position of the HUD")
                .defineInRange("hudX", 10, 0, Integer.MAX_VALUE);

        HUD_Y = BUILDER
                .comment("Y position of the HUD")
                .defineInRange("hudY", 10, 0, Integer.MAX_VALUE);

        COLOR_LOW = BUILDER
                .comment("Color for low latency (Green) in Hex")
                .defineInRange("colorLow", 0x00FF00, 0, 0xFFFFFF);

        COLOR_HIGH = BUILDER
                .comment("Color for high latency (Red) in Hex")
                .defineInRange("colorHigh", 0xFF0000, 0, 0xFFFFFF);

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
