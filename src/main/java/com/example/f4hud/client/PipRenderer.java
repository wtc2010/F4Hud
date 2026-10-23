package com.example.f4hud.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.function.Supplier;

import static com.example.f4hud.F4HudMod.LOGGER;

@EventBusSubscriber(modid = "f4hud", value = Dist.CLIENT)
public class PipRenderer {
    private static final int PIP_W = 320;
    private static final int PIP_H = 180;

    private static TextureTarget pipTarget;
    private static ResourceLocation pipTexLoc;
    private static boolean renderingPip;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        if (!PipState.active || renderingPip) {
            return;
        }

        ensureTarget();
        DeltaTracker partialTick = event.getPartialTick();
        float partial = partialTick.getGameTimeDeltaPartialTick(false);

        Camera camera = new Camera();
        camera.setup(mc.level, mc.player, true, false, partial);
        Vec3 camPos = camera.getPosition();

        float aspect = 320.0f / 180.0f;
        Matrix4f projection = new Matrix4f().setPerspective(
                (float) Math.toRadians(70.0d), aspect, 0.05f, 2000.0f);
        Matrix4f view = new Matrix4f().rotation(camera.rotation()).invert();
        view.translate(new Vector3f((float) -camPos.x, (float) -camPos.y, (float) -camPos.z));

        RenderTarget mainTarget = mc.getMainRenderTarget();

        renderingPip = true;
        try {
            pipTarget.bindWrite(false);
            RenderSystem.clearColor(0f, 0f, 0f, 0f);
            RenderSystem.clear(16640, true);
            event.getLevelRenderer().renderLevel(partialTick, false, camera,
                    mc.gameRenderer, mc.gameRenderer.lightTexture(), projection, view);
            renderingPip = false;
            pipTarget.unbindWrite();
        } catch (Throwable t) {
            LOGGER.error("[F4Hud] PIP render failed", t);
            renderingPip = false;
            pipTarget.unbindWrite();
        } finally {
            mainTarget.bindWrite(false);
        }
    }

    public static void draw(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (pipTarget == null || !PipState.active) {
            return;
        }

        ensureTextureRegistered(mc);
        int screenW = mc.getWindow().getGuiScaledWidth();
        int margin = 12;
        int w = Math.min(480, screenW / 3);
        int h = (int) (w * 0.5625f);
        int x = screenW - margin - w;
        int y = margin;

        try {
            guiGraphics.blit(pipTexLoc, x, y, 0f, 180.0f, w, -h, PIP_W, PIP_H);
            guiGraphics.fill(x - 2, y - 2, x + w + 2, y + h + 2, -1);
            guiGraphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, -16777216);
        } catch (Throwable t) {
            LOGGER.error("[F4Hud] PIP draw failed", t);
        }
    }

    private static void ensureTarget() {
        if (pipTarget == null) {
            pipTarget = new TextureTarget(PIP_W, PIP_H, false, false);
            pipTarget.setClearColor(0f, 0f, 0f, 0f);
            pipTarget.createBuffers(PIP_W, PIP_H, false);
        }
    }

    private static void ensureTextureRegistered(Minecraft mc) {
        if (pipTexLoc == null) {
            pipTexLoc = ResourceLocation.fromNamespaceAndPath("f4hud", "pip");
            mc.getTextureManager().register(pipTexLoc, new PipDynamicTexture(() -> pipTarget));
        }
    }

    private static class PipDynamicTexture extends AbstractTexture {
        private final Supplier<TextureTarget> target;

        private PipDynamicTexture(Supplier<TextureTarget> target) {
            this.target = target;
        }

        @Override
        public void load(ResourceManager resourceManager) {
        }

        @Override
        public int getId() {
            TextureTarget t = target.get();
            return t == null ? 0 : t.getColorTextureId();
        }

        @Override
        public void bind() {
            RenderSystem.bindTexture(getId());
        }
    }
}
