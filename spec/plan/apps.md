# 应用页实施方案

对应设计稿：`spec/ui/app.png`

## 1. 页面目标

应用页是整个项目里最有价值、也是最依赖系统权限的一页。它承担三件事：

- 面向应用维度查看使用情况
- 面向应用维度查看资源占用
- 提供单应用详情与系统设置跳转入口

## 2. 关键结论

- 应用列表、搜索、图标、名称、筛选、详情入口都可以实现。
- 使用时长、最近活跃、前台切换、流量、存储占用可以做，但绝大部分依赖 `PACKAGE_USAGE_STATS`。
- “前台切换次数”可做近似统计，不能保证和 OEM 系统统计完全一致。
- 页面底部当前选中应用信息卡可实现，但数据来源需要聚合多个系统服务。

## 3. 模块拆解与可行性

### 3.1 应用头部与搜索

设计稿内容：

- 页面标题
- 搜索框
- 通知按钮

实现建议：

- 通过 `PackageManager` 拉取安装应用
- 搜索在本地列表完成，不需要额外数据源

可行性评估：

- `A`

注意：

- Android 11+ 对包可见性有限制。如果只查询 launcher app，通常问题较小；如果后续要做更完整包扫描，需要检查 package visibility 配置。

### 3.2 分类筛选

设计稿内容：

- 常用
- 占空间
- 耗流量

实现建议：

- 常用：按区间使用时长排序
- 占空间：按 `StorageStatsManager.queryStatsForPackage()` 排序
- 耗流量：按 `NetworkStatsManager` 聚合到应用维度后排序

可行性评估：

- 常用：`B`
- 占空间：`B`
- 耗流量：`B`

限制：

- 都依赖使用情况访问权限
- 首屏要考虑“未授权”状态，而不是空白页

### 3.3 今日应用概览卡片

设计稿内容：

- 活跃应用数量
- 总使用时长
- 前台切换次数

实现建议：

- 活跃应用数量：区间内有使用记录且时长大于阈值的应用数
- 总使用时长：聚合所有应用使用时长
- 前台切换次数：通过 `UsageEvents` 中前后台相关事件做近似统计

可行性评估：

- 活跃应用数：`B`
- 总使用时长：`B`
- 前台切换：`B/C`

限制：

- 事件统计受系统事件粒度和 ROM 实现影响
- “前台切换 28 次”这种数字要视为统计值，不应宣称绝对准确

### 3.4 最常用应用列表

设计稿内容：

- 图标
- 名称
- 使用时长
- 最近活跃时间

实现建议：

- `UsageStatsManager` 提供 `lastTimeUsed` 与区间使用时长
- 应用图标和名称由 `PackageManager` 获取

可行性评估：

- `B`

### 3.5 当前应用信息卡片

设计稿内容：

- 今日使用
- 最近活跃
- 存储占用
- 今日流量
- 查看详情
- 系统设置

实现建议：

- 建立 `AppDetailAggregator`
- 聚合：
  - 使用时长
  - 最近活跃
  - 存储占用
  - Wi-Fi / 移动数据
- “系统设置”使用 `ACTION_APPLICATION_DETAILS_SETTINGS`
- “查看详情”进入应用详情页

可行性评估：

- `B`

## 4. 应用详情页建议补充

虽然设计稿只露出入口，但建议实现一个独立详情页，包含：

- 基础信息：图标、名称、包名
- 今日/近 7 天使用时长
- 最近活跃时间
- 存储拆分：app/data/cache
- 流量拆分：Wi-Fi / 移动数据
- 快捷操作：系统应用详情、通知设置、使用情况页

## 5. 数据与服务设计

建议拆出独立 system source：

- `InstalledAppSource`
- `AppUsageStatsSource`
- `AppStorageStatsSource`
- `AppNetworkStatsSource`

统一由 `AppsRepository` 对外提供聚合结果，避免页面自己拼数据。

## 6. 未授权状态必须单独设计

这一页不能默认假设权限已开通，至少需要这几种状态：

- 未授予使用情况访问权限
- 已授权，但尚未形成足够的趋势数据
- 数据加载中
- 正常展示

建议未授权时展示：

- 权限说明
- 一键跳转到 `ACTION_USAGE_ACCESS_SETTINGS`
- 明确告知开启后才能查看使用时长、流量与存储排行

## 7. 需要调整的预期

应用页的三个风险点必须在方案里写死：

- “前台切换次数”只能做系统统计口径下的近似值。
- “今日流量”与系统管家/OEM 工具可能存在口径差异。
- “最近活跃 xx 分钟前”依赖系统使用事件，不适合承诺秒级实时。

## 8. 参考资料

- [UsageStatsManager](https://developer.android.com/reference/android/app/usage/UsageStatsManager)
- [StorageStatsManager](https://developer.android.com/reference/android/app/usage/StorageStatsManager)
- [NetworkStatsManager](https://developer.android.com/reference/android/app/usage/NetworkStatsManager)
- [PackageManager](https://developer.android.com/reference/android/content/pm/PackageManager.html)
- [Settings.ACTION_APPLICATION_DETAILS_SETTINGS](https://developer.android.com/reference/android/provider/Settings.html)
