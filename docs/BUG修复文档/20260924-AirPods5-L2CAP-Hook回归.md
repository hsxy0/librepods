# AirPods 5 设置页无法显示：L2CAP Hook 回归排查

## 复现

安装兼容合并后的 `1.0.0-airpods5.3-debug (81)`，AirPods 5 连接后有弹窗，设置页仍为 `Tap to reconnect`。诊断日志 `D:\airpods_log_20260924_002937_e7084b4e.txt` 在 00:29:22 和 00:29:32 对 PSM `0x1001` 的连接两次报告 `Peer does not support our desired channel types`，随后 AACP 套接字超时，快照持续 `aacpConnected=false`。

## 定位

对照 `016133d`（兼容前）与 `e43f2d3`（兼容合并）：

- `BluetoothConnectionManager.kt` 的 L2CAP 套接字构造参数未变；`AirPodsService.kt` 仍使用 PSM `4097`，设置页依赖真实 AACP 连接状态。
- `l2c_fcr_hook.cpp` 原有 `fake_l2c_fcr_chk_chan_modes` Hook 会绕过蓝牙栈不兼容的通道模式检查。兼容合并新增 `isBluetoothProcess()`；当进程名不以 `com.android.bluetooth` 或 `com.google.android.bluetooth` 开头时，`native_init` 直接返回，Hook 不会安装。
- 新日志证明蓝牙栈实际运行了原始 `l2c_fcr_chk_chan_modes` 并报错；日志采集范围没有覆盖 Hook 初始化记录，因此进程名检查是高度可疑的回归点，但仍需新 APK 的设备验证来确认。

## 修复与验证

删除合并新增的进程名限制，恢复兼容前 `native_init` 行为。Java Xposed 入口已限定蓝牙包加载，原生回调仍只针对蓝牙库安装 Hook。保留 AirPods 5 型号素材和真实连接状态判断。

1. 安装修复后的 APK，并确保 LibrePods 的 Xposed 模块在蓝牙进程作用域启用；重启蓝牙进程或手机使原生 Hook 重新加载。
2. 连接 AirPods 5，确认诊断日志出现 `<LogCollector:Complete:Success> Socket connected`，快照 `aacpConnected=true`。
3. 打开设置页，检查设备型号、电量与控制项，并实际修改一个设置再读回。
4. 若仍失败，采集包含 `LibrePodsHook` 原生 Hook 日志和蓝牙 HCI snoop 的记录，区分未加载、符号定位失败与协商后续失败。
