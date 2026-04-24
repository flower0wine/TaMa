# 总览页实施方案

对应设计稿：`spec/ui/device.png`

## 1. 页面目标

总览页要解决的是“用户一进来就能看到设备当前状态和最值得关注的问题”。因此页面不是单纯展示原始数据，而是：

- 先展示设备状态摘要
- 再给出风险提示
- 最后落到最近活跃应用

## 2. 模块拆解与可行性

### 2.1 内存状态卡片

设计稿内容：

- 可用内存
- 总内存
- 使用比例进度条
- “过去 30 分钟持续下降”之类提示

实现建议：

- 通过 `ActivityManager.getMemoryInfo()` 获取 `availMem`、`totalMem`、`threshold`
- 使用本应用本地采样记录生成“过去 30 分钟趋势文案”

可行性评估：

- 当前内存快照：`A`
- 历史变化提示：`B`

风险与限制：

- Android 官方明确说明 `getMemoryInfo()` 更适合做全局内存状态判断，不建议高频轮询。
- 因此趋势提示不要做秒级刷新，建议按低频采样后聚合为趋势摘要。

### 2.2 电池与温度卡片

设计稿内容：

- 电量百分比
- 当前温度
- 热状态提示
- 底部温度波形

实现建议：

- 电量：`ACTION_BATTERY_CHANGED` / `BatteryManager`
- 温度：读取 `BatteryManager.EXTRA_TEMPERATURE`，按 `value / 10f` 转成摄氏度
- 热状态：优先使用 `PowerManager.getCurrentThermalStatus()`，API 29+ 可监听热状态变化
- 波形：本地采样后自绘折线

可行性评估：

- 电量：`A`
- 电池温度：`B`
- 热状态分级：`B`
- OEM 级“机身温度曲线”：`C`

风险与限制：

- `EXTRA_TEMPERATURE` 更接近电池温度，不等于整机表面温度。
- `PowerManager` 热状态在不同设备上支持度和灵敏度不一致，不能保证所有机型都精确反映“发热程度”。
- 因此 UI 文案建议写成“电池温度”“热状态估计”，不要写成绝对结论。

### 2.3 今日流量卡片

设计稿内容：

- Wi-Fi
- 移动数据

实现建议：

- 设备级汇总可使用 `NetworkStatsManager.querySummaryForDevice()` 或 `querySummaryForUser()`
- 首版建议优先做“用户级/设备级今日总览”，不要承诺精确到每个后台细节

可行性评估：

- 今日流量汇总：`B`

风险与限制：

- 设备级和跨应用统计依赖 `PACKAGE_USAGE_STATS`
- 某些统计维度在不同 ROM 上可能存在延迟或口径差异
- 设计稿如果要求和系统设置页完全一致，预期要下调

### 2.4 存储空间卡片

设计稿内容：

- 已用
- 可用
- 总容量

实现建议：

- 使用 `StorageStatsManager.getFreeBytes()` 和 `getTotalBytes()`
- 展示为“面向用户的容量视图”

可行性评估：

- `A`

风险与限制：

- 官方说明这些值适合面向用户展示，不适合拿来做严格逻辑判断。

### 2.5 当前建议卡片

设计稿内容：

- 如“温度偏高，建议停止边充边玩”

实现建议：

- 采用规则引擎而非 AI
- 输入来源：
  - 热状态
  - 电池温度
  - 可用内存阈值
  - 流量异常
  - 高占用应用
- 输出固定模板建议文案

可行性评估：

- `A`

建议规则示例：

- 温度或热状态偏高时给出散热建议
- 存储占用超过阈值时给出清理建议
- 未开启使用情况访问权限时给出授权建议

### 2.6 今日最活跃应用

设计稿内容：

- 活跃应用列表
- 使用时长

实现建议：

- 使用 `UsageStatsManager` 聚合当天应用使用信息
- 图标与名称通过 `PackageManager` 读取

可行性评估：

- `B`

风险与限制：

- 依赖 `PACKAGE_USAGE_STATS`
- 某些系统会对统计时间粒度做聚合，不能保证实时秒级准确

## 3. 建议的页面状态模型

建议用一个聚合状态驱动页面：

```kotlin
data class OverviewUiState(
    val memory: MemoryCardUiModel,
    val battery: BatteryCardUiModel,
    val network: NetworkCardUiModel,
    val storage: StorageCardUiModel,
    val insight: InsightUiModel?,
    val topApps: List<ActiveAppUiModel>,
    val permissionState: OverviewPermissionState,
    val isLoading: Boolean
)
```

## 4. 页面落地顺序

建议按这个顺序开发：

1. 先完成静态 UI 骨架与设计系统组件
2. 接入内存、电量、存储等可直接读取的数据
3. 加入使用情况访问权限引导
4. 接入活跃应用与流量摘要
5. 最后加入趋势提示与建议规则

## 5. 需要调整的预期

建议对总览页的两个点做产品调整：

- “温度”统一改为“电池温度 / 热状态”
- “今日流量”改为“系统统计口径下的今日流量”

## 6. 参考资料

- [ActivityManager.MemoryInfo](https://developer.android.com/reference/android/app/ActivityManager.MemoryInfo.html)
- [ActivityManager.getMemoryInfo](https://developer.android.com/reference/kotlin/android/app/ActivityManager)
- [BatteryManager](https://developer.android.com/reference/android/os/BatteryManager.html)
- [PowerManager Thermal APIs](https://developer.android.com/reference/android/os/PowerManager.html)
- [NetworkStatsManager](https://developer.android.com/reference/android/app/usage/NetworkStatsManager)
- [StorageStatsManager](https://developer.android.com/reference/android/app/usage/StorageStatsManager)
- [UsageStatsManager](https://developer.android.com/reference/android/app/usage/UsageStatsManager)
