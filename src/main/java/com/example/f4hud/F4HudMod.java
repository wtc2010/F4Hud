package com.example.f4hud;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(F4HudMod.MOD_ID)
public class F4HudMod {
    public static final String MOD_ID = "f4hud";
    public static final Logger LOGGER = LoggerFactory.getLogger(F4HudMod.class);

    public F4HudMod(IEventBus modEventBus) {
    }
}
