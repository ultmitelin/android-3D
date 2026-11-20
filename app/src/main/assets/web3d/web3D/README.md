# 3D模型横向对比查看器

一个**单文件 HTML** 的 3D 模型横向对比页面，用于将同一张参考图在多个 3D 生成方案
（Pixal3D、Pixal3D + HY3D 贴图、Rodin Gen2.5-High、Tripo V3.1、混元3D、Pixal3D GGUF）
下的生成结果进行并排对比。

**在线访问**：<https://ultmitelin.github.io/3Dmodel--comparison/3d-model-comparison.html>

## 功能特性

- **5 组对比**：每组包含 1 张参考图 + 6 个方案模型，参考图打开页面即自动载入
- **按需加载**：第 1 组自动载入模型；第 2–5 组点击 `Load 3D` 后才加载 GLB，
  全部加载完成后按钮变为红色 `✕ Close`，点击即可释放该组模型、节省显存
- **同步相机**：拖动任意模型所有卡片同步旋转，滚轮 / 双指缩放，双击重置视角
- **灰模 / 纹理分割线**：每张卡片左侧为灰模（几何）、右侧为纹理，
  开启"同步分割条"后拖动任一分割线全部联动
- **法线视图**：单卡或全局开启 Normal 开关，使用 three.js `MeshNormalMaterial`
  叠加渲染法线视图，与当前相机实时同步（缩放比例与贴图视图严格一致）
- **全屏查看**：第 1 组每张卡片右上角 `⛶` 按钮可单卡全屏
- **多模型对比**：第 1 组通过卡片右上角的空心圆圈选中 2–3 个模型，
  点击工具栏"对比"按钮进入全屏分屏对比；`Esc` 或"✕ 退出对比"返回，
  点击卡片外空白区域可取消全部选中
- **自定义模型**：第 1 组支持输入模型 URL 或上传本地 `.glb` 文件实时替换
- **模型旋转修正**：每张卡片提供 `↻90°` 按钮，用于修正朝向不一致的模型

## 目录结构

```
├── 3d-model-comparison.html   # 主页面（全部逻辑都在这一个文件里）
├── libs/                      # 本地依赖（无需联网）
│   ├── model-viewer.min.js    # Google <model-viewer>
│   ├── three.module.js        # three.js（法线视图渲染）
│   └── addons/                # GLTFLoader 等 three.js 插件
├── models/                    # 模型与参考图资源
│   ├── group-01/ … group-05/  # 每组：reference.(png|jpg) + 6 个 .glb
├── 启动3D对比页面.bat          # Windows 一键启动本地服务器并打开页面
├── requirements.txt           # Python 依赖说明（仅标准库，无需安装）
└── README.md
```

## 本地运行

由于浏览器对 `file://` 协议的模块与跨域限制，需要通过 HTTP 服务器访问。

**方式一（Windows）**：双击 `启动3D对比页面.bat`，会自动启动本地服务器并打开页面。

**方式二（任意系统，需要 Python 3）**：

```bash
python -m http.server 8000
```

然后浏览器访问 <http://localhost:8000/3d-model-comparison.html>。

> 无需安装任何第三方依赖：页面运行只依赖浏览器，
> 本地服务器只用到 Python 标准库的 `http.server`。

## 替换 / 新增模型

1. 将 `.glb` 文件放入对应的 `models/group-0X/` 目录（推荐单文件 `.glb`，
   避免外部纹理路径失效）
2. 打开 `3d-model-comparison.html`，搜索 `PRESET`，
   修改对应组的 `image` / `models` / `orientations` 路径即可
3. 模型朝向不一致时，可在 `orientations` 中按 `"0deg 0deg 90deg"` 格式逐个修正

## 技术说明

- 基于 [Google model-viewer](https://modelviewer.dev/) 渲染贴图 / 灰模视图，
  灰模通过运行时移除 PBR 纹理生成
- 法线视图由 [three.js](https://threejs.org/) 独立渲染，
  每帧读取 model-viewer 的实时相机距离与 FOV 保持严格同步
- 所有依赖均已本地化到 `libs/`，离线可用
- 若模型来源跨域，请确保服务器允许 CORS

## 许可

模型与参考图仅用于方案效果对比展示，请勿用于其他用途。
