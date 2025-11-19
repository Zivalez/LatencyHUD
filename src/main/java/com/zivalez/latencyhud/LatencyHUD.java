package com.zivalez.latencyhud;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(LatencyHUD.MODID)
public class LatencyHUD {
    public static final String MODID = "latencyhud";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LatencyHUD(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
