package com.example.f4hud.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import static com.example.f4hud.F4HudMod.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class HudOverlay {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (KeyBindings.TOGGLE_HUD.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (!PipState.active) {
                // 刚打开：捕获当前相机位姿，并重置一次性渲染标记（下次渲染一帧静态画面）
                Camera cam = mc.gameRenderer.getMainCamera();
                PipState.capture(cam.getPosition(), cam.getYRot(), cam.getXRot());
                PipRenderer.resetOnToggle();
            }
            PipState.active = !PipState.active;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (PipState.active) {
            PipRenderer.draw(event.getGuiGraphics());
        }
    }
}
