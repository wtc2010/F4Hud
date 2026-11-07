# F4Hud

**给 Minecraft 建筑与机械整合包用的画中画小窗。**

按 `F4`，在屏幕角落开一个锁定机位的小窗口；按 `K` 调参数。窗口可以冻结成一张快照，也可以持续刷新——你可以走开、转头、去干别的，那个角落里的世界还在动。

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62b47a)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1%2B-da9a5a)
![Side](https://img.shields.io/badge/side-client--only-555)
![License](https://img.shields.io/badge/license-MIT-blue)

> 仅客户端模组。装在服务器上没有意义，也不会影响他人。

---

## 它解决什么问题

建筑时想对照远处的参考结构，机械整合包里想盯着某台机器运转，录制时想要一个“观众视角”——原版只能靠反复转头或切 F5，很打断节奏。

F4Hud 把「某一瞬间的视角」钉在屏幕角落：机位不动，画面可以是静止的，也可以是活的。

## 两种模式

| 模式 | 行为 | 开销 |
| --- | --- | --- |
| **冻结快照**（默认） | 按 F4 那一刻把当前画面截下来贴进小窗，之后完全静止 | 几乎为零 |
| **实时快照** | 机位锁定在按 F4 时的位置与朝向，画面持续重绘——生物在走、机器在转、方块在变，但你转头走开它不动 | 每秒重复渲染若干次世界（默认 10 次/秒） |

两者的差别是「当时看到的那一帧」和「从当时那个机位继续看下去」。后者才是这个模组真正的用途：站在远处把刷怪区、熔炉组、红石时钟钉在角落，然后自己去干活。

## 特性

- 四角锚定，宽度、边距、FOV、不透明度全部可调
- 可选的 2px 描边，方便在亮色背景上辨认边界
- 按下时自动预热，等地形真正渲染完成再取画面（避免小窗里一片空洞）
- 与 F1 隐藏 GUI 联动（可关）
- 实验性「画面里显示自己」：机位离角色超过 2.5 格时让玩家模型入镜
- 配置持久化到 `config/f4hud-client.json`，也可从模组列表打开设置
- 无 Mixin、无额外前置，本体约 30 KB

## 安装

1. 安装 **NeoForge 21.1.x**（Minecraft 1.21.1）。
2. 把 `f4hud-<version>.jar` 放进 `.minecraft/mods/`。
3. 启动游戏。

支持范围：Minecraft `1.21.1 – 1.21.x`、NeoForge `21.1.0+`、客户端。

## 使用

### 按键

| 按键 | 功能 |
| --- | --- |
| `F4` | 开关小窗（开的时候捕获当前机位） |
| `K` | 打开设置界面（没有其它界面打开时生效） |

两个按键都可以在「选项 → 控制」里改绑，分类为 `F4Hud`。

### 设置界面

| 选项 | 默认 | 范围 |
| --- | --- | --- |
| 位置 | 右上角 | 右上 / 左上 / 右下 / 左下 |
| 小窗宽度（占屏宽） | 30% | 10–90% |
| 水平 / 垂直边距 | 12 px | 0–200 px |
| 画面视野 FOV | 70 | 30–110 |
| 不透明度 | 100% | 15–100% |
| 边框 | 开 | 开 / 关 |
| 画面模式 | 冻结快照 | 冻结 / 实时 |
| 刷新间隔 | 100 ms | 33–500 ms |
| F1 隐藏时一起隐藏 | 开 | 开 / 关 |
| 画面里显示自己 | 关 | 开 / 关（实验性） |

### 配置文件

`<游戏目录>/config/f4hud-client.json`，自动生成，可在游戏外直接改：

```json
{
  "anchor": "TOP_RIGHT",
  "widthPercent": 30,
  "offsetX": 12,
  "offsetY": 12,
  "border": true,
  "opacity": 1.0,
  "fov": 70.0,
  "live": false,
  "updateIntervalMs": 100,
  "hideWithGui": true,
  "showPlayer": false
}
```

| 字段 | 说明 | 读入时的约束 |
| --- | --- | --- |
| `anchor` | `TOP_RIGHT` / `TOP_LEFT` / `BOTTOM_RIGHT` / `BOTTOM_LEFT` | 非法值回落到右上 |
| `widthPercent` | 小窗宽度占屏宽百分比 | 10–90 |
| `offsetX` / `offsetY` | 距屏幕边缘的像素边距 | 0–400 |
| `border` | 是否绘制描边 | — |
| `opacity` | 不透明度 | 0.15–1.0 |
| `fov` | 小窗相机视野 | 30–110 |
| `live` | `true` 为实时快照，`false` 为冻结快照 | — |
| `updateIntervalMs` | 实时模式的刷新间隔 | 33–2000 |
| `hideWithGui` | F1 隐藏 GUI 时是否一起隐藏 | — |
| `showPlayer` | 是否让玩家模型出现在画面中（离机位 > 2.5 格时生效） | — |

## 兼容性

| 模组 | 方式 | 行为 |
| --- | --- | --- |
| **Sodium** | 反射检测地形是否渲染完成 | 未完成时延长预热，避免空画面 |
| **EntityCulling** | 反射临时关闭剔除 | 小窗渲染期间暂停剔除，避免实体闪烁 |
| **CamUtilities** | 反射读取分离相机状态 | 处于自定义视角时以该机位为准 |

三者均为**软依赖**：没装就静默跳过，检测失败会降级并记录一条 warn，不会导致崩溃。

### 调试开关

排障时可在 JVM 参数里加：

| 参数 | 作用 |
| --- | --- |
| `-Df4hud.debugLog=true` | 每秒输出一次小窗相机状态（机位、距离、是否分离相机） |
| `-Df4hud.debugMainCamera=true` | 直接用主相机，跳过自建相机（用于判断问题是否出在机位上） |
| `-Df4hud.debugNoCullFrustum=true` | 不替换视锥（画面可能出现不该出现的区块，用于确认剔除问题） |
| `-Df4hud.debugNoEcCompat=true` | 不暂停 EntityCulling |

## 已知限制

- 小窗内部固定 **320×180** 渲染，再按窗口尺寸放大，边缘会偏糊、有锯齿。
- 画面中**不含**手持物品、HUD 元素与方块选中框。
- 实时模式等于“每秒多渲染 N 次世界”，在大型整合包里建议把刷新间隔调到 200–500 ms，或使用冻结模式。
- 依赖反射访问 `Camera` 的私有方法与其它模组的内部字段，游戏或相关模组更新后可能失效；失效时会降级成提示或实体闪烁，不会崩溃。
- 未针对光影（Iris/Oculus）做专门适配，开启光影时的小窗画面请自行验证。
- 与其它在 `AFTER_PARTICLES` 阶段做渲染的模组理论上存在状态互相干扰的可能。

## 实现简介

小窗不是截图贴图，而是在渲染管线里再跑一遍世界渲染：

1. 记录机位（位置、yaw、pitch），必要时从 CamUtilities 读取分离相机。
2. 在 `RenderLevelStageEvent` 的 `AFTER_PARTICLES` 阶段保存主帧缓冲、清屏。
3. 自建 `Camera` 与投影矩阵（FOV 可调，近 0.05 / 远 2000，16:9），替换模型视图矩阵与视锥。
4. 暂停 EntityCulling，调用 `LevelRenderer.renderLevel(...)` 重绘世界（不画选中框）。
5. 结果 blit 到离屏纹理，恢复主画面，上传为 `DynamicTexture`。
6. 在 HUD 阶段按锚点与边距贴出，并按不透明度着色。

全程基于 NeoForge 事件与反射，不使用 Mixin。

## 从源码构建

需要 JDK 21。

```bash
./gradlew build
# Windows
gradlew.bat build
```

产物位于 `build/libs/`，取不带 `-sources` 的那个 jar。

## 更新日志

### 1.1.4

- **修正（关键）**：第二次渲染前清空并重置模型视图矩阵栈、替换为小窗自己的投影与剔除。此前栈顶残留主视角旋转，导致小窗内生物、玩家、箱子与木桶以错误变换绘制——表现为“窗口里看不到生物/玩家、晃视角时方块忽隐忽现”。
- 第二次渲染期间暂停 EntityCulling 剔除，并先清颜色再绘制。
- 设置中新增「画面里显示自己」（实验性，默认关闭）。

## 反馈

问题请提到 [Issues](https://github.com/wtc2010/F4Hud/issues)，附上游戏版本、NeoForge 版本、模组列表与 `latest.log`；渲染相关问题请先说明是否使用光影。

## 许可证

[MIT](LICENSE) © wtc2010

---

## English

**F4Hud** is a client-side picture-in-picture overlay for Minecraft 1.21.1 (NeoForge 21.1+).

Press `F4` to pin a window in a screen corner showing the world from the camera pose captured at that moment. Two modes: **frozen** (a single still frame) and **live** (the pose stays fixed while the scene keeps updating — mobs move, machines run, blocks change, and it does not follow your view). Press `K` to configure position, width, FOV, opacity, border, mode, refresh interval, and an experimental self-visibility option. Settings are stored in `config/f4hud-client.json`.

Soft-compatible with Sodium, EntityCulling and CamUtilities via reflection; no Mixin, no extra dependencies. Client-only, MIT licensed.

```bash
./gradlew build   # requires JDK 21
```
