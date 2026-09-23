# BUG 修复报告

## 基本信息

- 日期：2026-09-23
- 报告人：Codex
- 模块/功能：Android FOSS Debug CI 打包、Xposed API 100 模块
- 严重级别：高（阻断 APK 构建与产物上传）

## 问题描述

- 期望结果：`assembleFossDebug` 生成 APK，后续单测、Lint、产物上传正常执行。
- 实际结果：GitHub Actions 运行 `35800381055` 在 `:app:processFossDebugResources` 的依赖解析阶段失败，找不到 `io.github.libxposed:api:100.0.0`。
- 受影响提交：`00f061e`，运行地址：<https://github.com/hsxy0/librepods/actions/runs/35800381055>。

## 复现步骤

1. 在提交 `00f061e` 的 `android` 目录执行 `./gradlew --no-daemon assembleFossDebug`（或查看上述同提交 CI 运行）。
2. Gradle 解析 `:app:fossDebugCompileClasspath`。
3. 观察 `Could not find io.github.libxposed:api:100.0.0`，没有 APK 产物。

## 复现环境

- GitHub Actions：Ubuntu runner、JDK 21、Android 37.0、NDK 30.0.14904198、CMake 3.22.1。
- 本机：Windows，JDK 17；未找到 Android SDK，无法在此环境复现完整 Gradle APK 构建。
- Maven Central 元数据只列出 libxposed API `101.0.0`、`101.0.1` 和 `102.0.0`；API 100 未发布。

## 定位过程与根因

- `android/gradle/libs.versions.toml` 将 API 指向 `100.0.0`，但此工件不在已配置仓库中。
- 进一步核对 API 100 源码发现，当前 `KotlinModule` 使用了错误的单参数构造函数、API 101 的 `intercept` Hook 写法、`moduleApplicationInfo` / `apiVersion` 属性和三参数 `log` 调用。仅补一个依赖源仍会在编译阶段失败。

## 修复方案与变更

- 将固定来源的 API 100 Java 接口放入本地 `:xposed-api` 模块；App 以 `compileOnly(project(":xposed-api"))` 使用，避免把框架提供的 API 装入 APK。
- 将模块入口构造函数、Hooker 回调、模块应用信息及日志调用调整为 API 100 契约。
- 保持 app 侧已发布的 `libxposed:service:101.0.0` 不变。
- 加入来源与许可证说明，以及两项 API 契约回归测试。
- 风险：远端构建已通过，但尚未在真实 Xposed 设备上验证运行时行为。回滚方式是回退本修复提交；但原始 API 100 依赖错误会恢复。

## 验证结果

- `python -m unittest discover -s android/tests -p test_xposed_api_contract.py -v`：2 项通过。
- `git diff --check`：通过。
- 修复分支 GitHub CI：[运行 35812983200](https://github.com/hsxy0/librepods/actions/runs/35812983200) 结论为 `success`；`Build FOSS debug APK`、`Run JVM unit tests`、`Run Android Lint`、checksum 与上传步骤均成功。
- APK artifact：`apk-foss-debug-35812983200`，GitHub API 显示大小 23,290,794 字节；报告 artifact 也已上传。
- `main` 提交 `54935d8` 的 [运行 35813462742](https://github.com/hsxy0/librepods/actions/runs/35813462742) 结论为 `success`；上述五个步骤全部成功。
- `main` APK artifact：`apk-foss-debug-35813462742`，GitHub API 显示大小 23,290,793 字节；报告 artifact 也已上传。
- 本机未安装 Android SDK，完整 APK 构建由上述 GitHub CI 验证。

## 手动验证步骤

1. 打开 **Actions → Android Debug CI**，找到修复分支运行 `35812983200`。
2. 打开 **Build FOSS debug APK**，确认依赖解析通过且 `assembleFossDebug` 以 `BUILD SUCCESSFUL` 结束。
3. 检查 **Run JVM unit tests** 与 **Run Android Lint** 均成功。
4. 在运行页面下载 APK artifact，确认包含 `app-foss-debug.apk`，并核对 checksum 文件。
5. 在支持 API 100 的 Xposed 框架设备安装 APK，启用模块，观察模块初始化、Bluetooth 设置图标 Hook 和远程首选项功能。
6. 合入 `main` 后再次检查 `main` 对应 Actions 运行及 APK artifact。

## 遗留问题

- 在 API 100 Xposed 设备上执行上述运行时手动验证。
