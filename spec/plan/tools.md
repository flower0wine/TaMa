# 工具页实施方案

对应设计稿：`spec/ui/tool.png`

## 1. 页面目标

工具页本质上是“建议中心 + 快捷入口 + 权限状态页”。这一页最容易做出完整体验，因为它更多依赖状态判断和系统设置跳转，而不是底层受限数据。

## 2. 模块拆解与可行性

### 2.1 智能建议

设计稿内容：

- 温度偏高建议
- 存储清理建议
- 使用分析未开启建议

实现建议：

- 使用规则引擎输出建议卡片
- 每张建议卡包含：
  - 图标
  - 标题
  - 原因描述
  - CTA 文案
  - 对应 intent/action

可行性评估：

- `A`

建议卡来源示例：

- 热状态偏高
- 应用占空间过大
- 使用情况访问未授权
- 通知权限未开启
- 悬浮窗未开启

### 2.2 快捷入口

设计稿内容：

- 电池优化
- 应用详情
- 存储管理
- 通知管理
- 使用情况访问
- 网络用量

实现建议：

- 优先跳系统设置页，而不是尝试直接做“代替系统设置”的能力
- 统一封装 `SettingsNavigator`

可行性评估：

- `A/B`

建议映射：

- 电池优化：设备电池相关设置页或应用电池设置页
- 应用详情：应用列表页或系统应用详情页
- 存储管理：系统存储设置 / 应用详情存储页
- 通知管理：`ACTION_APP_NOTIFICATION_SETTINGS`
- 使用情况访问：`ACTION_USAGE_ACCESS_SETTINGS`
- 网络用量：`ACTION_DATA_USAGE_SETTINGS`

### 2.3 权限状态

设计稿内容：

- 使用情况访问
- 通知权限
- 悬浮窗

实现建议：

- 使用情况访问：通过 `AppOps` / 可用性探测判断
- 通知权限：`NotificationManager.areNotificationsEnabled()`
- 悬浮窗：`Settings.canDrawOverlays()`

可行性评估：

- `A/B`

限制：

- 悬浮窗在 Android 11+ 的授权跳转行为与旧版本不同
- Android Go 设备上悬浮窗权限可能直接不可用

### 2.4 底部说明卡片

设计稿内容：

- “本应用不会直接结束其他应用后台进程”

实现建议：

- 建议保留，并加强表达
- 这能主动管理用户预期，避免把 App 误解成真正的系统管家

可行性评估：

- `A`

## 3. Settings 导航策略

建议统一封装：

```kotlin
interface SettingsNavigator {
    fun openUsageAccess()
    fun openAppNotificationSettings(packageName: String)
    fun openOverlaySettings()
    fun openAppDetails(packageName: String)
    fun openDataUsageSettings()
}
```

原因：

- 各 Android 版本对设置跳转支持不完全一致
- 集中兜底更容易维护

## 4. 明确不可做或不建议做的点

工具页里有些按钮不要误导成“直接执行系统级操作”：

- 不建议承诺“点击后直接清理微信缓存”
- 不建议承诺“直接关闭后台应用”
- 不建议承诺“直接开启某权限”

更合理的方式是：

- 给出原因
- 给出系统设置入口
- 让用户在系统页面完成授权或处理

## 5. 需要调整的预期

这页有一个需要尽早确认的方向问题：

- 如果产品目标是“系统管家”，很多按钮会被用户期待成“一键优化”；第三方应用无法稳定做到。
- 如果产品目标是“分析与建议工具”，那么工具页完全成立，而且体验会更真实可信。

## 6. 参考资料

- [NotificationManager.areNotificationsEnabled](https://developer.android.com/reference/android/app/NotificationManager.html)
- [Settings API Reference](https://developer.android.com/reference/android/provider/Settings.html)
- [UsageStatsManager](https://developer.android.com/reference/android/app/usage/UsageStatsManager)
- [NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService.html)
- [Android 11 overlay permission changes](https://developer.android.com/about/versions/11/privacy/permissions)
