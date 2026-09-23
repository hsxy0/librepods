# beidaoc 分支兼容移植

## 来源与范围

- 来源：<https://github.com/beidaoc/librepods>，共同基线 `b5a3eae`，移植截至 `2aad9c3` 的 9 个独有提交。
- 移植内容：心率协议与 Keep 桥接、空间音频及头部追踪、小米蓝牙与 MiLink 适配、通知播报、诊断日志和 ATT 重连机制。
- 对应代码主要位于 `android/app/src/main/java/me/kavishdevar/librepods/{bluetooth,keepbridge,milink,notifications,services,utils,xiaomifix}`；上游测试一并保留。

## 本项目的兼容决策

- 继续使用本地 `:xposed-api` **API 100** 编译模块，App 以 `compileOnly(project(":xposed-api"))` 引用；框架 API 不装入 APK。App 侧 `libxposed:service:101.0.0` 是独立依赖。
- 上游新增模块原本使用 API 101 的 `hook(...).intercept(...)`。`Api100Interception` 将这些拦截器接到 API 100 的 `Hooker.before` 与 `invokeOrigin`，MiLink、Xiaomi Fix 和 Keep 模块入口改用 API 100 双参数构造函数。
- AirPods 5 保留原有设备能力与听力功能边界：ATT 仅对支持相关能力的型号、Pro 1 自定通透或未知型号尝试连接；AirPods 5 不开启不需要的 ATT 通道。新 ATT 恢复逻辑仍遵循用户开关。
- 保留本项目的睡眠定时器和重复连接保护，并合入上游心率监测指令与连接恢复逻辑。
- 发布标识为 `1.0.0-airpods5.3`、`versionCode=81`，高于上游的 80，可按同包名升级；根模块压缩包加入上游的头部追踪辅助程序。

## 验证与设备检查

- `python -m unittest discover -s android/tests -p test_xposed_api_contract.py -v` 验证 API 100 编译依赖和模块入口。
- GitHub Actions [运行 35850781253](https://github.com/hsxy0/librepods/actions/runs/35850781253) 在提交 `857ce2f` 上为 `success`：`assembleFossDebug`、`zipDebugModule`、JVM 单测、`lintFossDebug` 和两项产物上传均通过。产物为 `apk-foss-debug-35850781253`、`root-module-debug-35850781253`，各自包含 SHA-256 清单。
- 真机仍需检查：AirPods 5 连接与 ANC/通透控制、Pro 1 自定通透、心率读取及 Keep 桥接、小米蓝牙/MiLink、空间音频头部追踪、通知播报和 root 模块安装。尤其要确认目标 Xposed 框架对 API 100 构造函数及回调的实际执行。
