package com.example.f4hud.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class KeyBindings {
    public static final String CATEGORY = "category.f4hud";

    public static final KeyMapping TOGGLE_HUD = new KeyMapping(
            "key.f4hud.toggle",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F4,
            CATEGORY
    );

    private KeyBindings() {
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_HUD);
    }
}
