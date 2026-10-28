package com.example.f4hud.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
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
    private static boolean pipFailedOnce;

    private static boolean hasIris() {
        ModList modList = ModList.get();
        return modList.isLoaded("iris") || modList.isLoaded("oculus");
    }

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
        // 之前已经在 Iris 下渲染失败过，就不再每帧重试（避免刷错误/卡顿），
        // 由 draw() 显示提示框。
        if (pipFailedOnce) {
            return;
        }

        ensureTarget();
        DeltaTracker partialTick = event.getPartialTick();

        // 用 F4 按下时捕获的固定位姿构建相机矩阵，而不是玩家当前视角。
        float yawRad = PipState.camYaw * ((float) Math.PI / 180f);
        float pitchRad = PipState.camPitch * ((float) Math.PI / 180f);
        Quaternionf camRot = new Quaternionf().rotateYXZ(-yawRad, -pitchRad, 0.0f);

        float aspect = (float) PIP_W / (float) PIP_H;
        Matrix4f projection = new Matrix4f().setPerspective(
                (float) Math.toRadians(70.0d), aspect, 0.05f, 2000.0f);
        Matrix4f view = new Matrix4f().rotation(camRot).invert();
        view.translate(new Vector3f((float) -PipState.camX, (float) -PipState.camY, (float) -PipState.camZ));

        RenderTarget mainTarget = mc.getMainRenderTarget();

        renderingPip = true;
        try {
            pipTarget.bindWrite(false);
            RenderSystem.clearColor(0f, 0f, 0f, 0f);
            RenderSystem.clear(16640, true);
            // 用一个临时相机对象（位置/朝向由上面的 view 矩阵决定）。
            // 直接复用玩家相机实例即可，渲染管线只读取传入的 projection/view。
            event.getLevelRenderer().renderLevel(partialTick, false, mc.gameRenderer.getMainCamera(),
                    mc.gameRenderer, mc.gameRenderer.lightTexture(), projection, view);
            renderingPip = false;
            pipTarget.unbindWrite();
        } catch (Throwable t) {
            LOGGER.warn("[F4Hud] PIP render failed (likely Iris incompat), showing hint", t);
            renderingPip = false;
            pipFailedOnce = true;
            try { pipTarget.unbindWrite(); } catch (Throwable ignored) {}
        } finally {
            mainTarget.bindWrite(false);
        }
    }

    public static void draw(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        if (!PipState.active) {
            return;
        }

        int screenW = mc.getWindow().getGuiScaledWidth();
        int margin = 12;
        int w = Math.min(480, screenW / 3);
        int h = (int) (w * 0.5625f);
        int x = screenW - margin - w;
        int y = margin;

        if (pipTarget == null || pipFailedOnce) {
            // 画面没渲染出来（Iris 下二次渲染不可用），显示提示框
            try {
                guiGraphics.fill(x - 2, y - 2, x + w + 2, y + h + 2, -1);
                guiGraphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, -16777216);
                String msg = hasIris() ? "F4Hud: 光影下画中画渲染不可用" : "F4Hud: 画中画渲染未就绪";
                guiGraphics.drawString(mc.font, msg, x + 6, y + 6, 0xFFFFFF, false);
            } catch (Throwable t) {
                LOGGER.error("[F4Hud] PIP hint draw failed", t);
            }
            return;
        }

        ensureTextureRegistered(mc);
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
