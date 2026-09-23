# AirPods 5 弹窗型号与设置页不可用

## 复现和证据

设备：OnePlus PJZ110、Android 15，应用 `1.0.0-airpods5.3-debug (81)`。连接 AirPods 后弹窗播放 AirPods Pro 视频，设置页显示 `Tap to reconnect`。诊断日志 `D:\airpods_log_20260923_234556_a00de1d3.txt`：

- 23:46:00.963 收到 `ACL_CONNECTED`，随后弹窗出现。
- 23:46:00.995 蓝牙栈注册 PSM `0x1001`，接着报告 `Peer does not support our desired channel types`。
- 23:46:05.993 AACP 套接字仍处于 `INIT`，应用超时关闭；`aacpConnected=false`。经典蓝牙音频连接不代表 AACP 控制通道已建立。

## 定位和处理

AirPods 5 的 `connectionArtworkRes` 曾被设为 `null`，于是连接弹窗回退到通用的 AirPods Pro 视频。现恢复为与主界面一致的设备轮廓，并对弹窗和灵动岛中的图片增加持续的位移与缩放动效，关闭时停止动画。

`Tap to reconnect` 由实际 AACP 套接字状态决定。上述日志显示 L2CAP 模式协商失败，不能用界面标志绕过，否则设置项会出现却无法控制耳机。Android 蓝牙栈报告的模式不兼容不由当前应用的超时逻辑导致；仍需在这台手机上验证是否可通过系统蓝牙栈、设备固件或可用的底层模块建立 PSM `0x1001` 通道。当前代码改动不声称已修复此底层连接。

## 验证

1. 安装新 APK，清除演示模式，断开并重新连接 AirPods 5。
2. 确认弹窗和灵动岛显示开放式耳塞与充电盒轮廓，图片持续轻微运动，不再显示 Pro 耳塞视频。
3. 导出诊断日志，确认是否出现 `<LogCollector:Complete:Success> Socket connected`，并且快照为 `aacpConnected=true`。只有这一项通过后，才验证设置页面及实际控制读回。
4. 若仍出现 `Peer does not support our desired channel types`，保存带 HCI snoop 的系统蓝牙日志，用于区分手机蓝牙栈配置与耳机固件协商行为。
