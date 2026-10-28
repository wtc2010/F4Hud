package com.example.f4hud.client;

import net.minecraft.world.phys.Vec3;

/**
 * 画中画状态：记录 F4 按下那一刻玩家相机的位置与朝向。
 * 之后小窗始终从这个固定位姿渲染，玩家怎么移动、转视角都不影响小窗。
 */
public final class PipState {
    public static boolean active = false;

    /** F4 按下时捕获的相机世界坐标 */
    public static double camX;
    public static double camY;
    public static double camZ;
    /** F4 按下时捕获的相机朝向（角度） */
    public static float camYaw;
    public static float camPitch;

    private PipState() {
    }

    public static void capture(Vec3 pos, float yaw, float pitch) {
        camX = pos.x;
        camY = pos.y;
        camZ = pos.z;
        camYaw = yaw;
        camPitch = pitch;
    }
}
