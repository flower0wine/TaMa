# 分析页实施方案

对应设计稿：`spec/ui/analyze.png`

## 1. 页面目标

分析页不是“即时状态页”，而是“趋势页”。这意味着它依赖历史数据，而历史数据并不会由 Android 为第三方 App 完整准备好。

所以这一页的核心不是图表本身，而是：

- 采样策略
- 聚合策略
- 数据可信度表达

## 2. 关键结论

- 折线图、柱状图、环形图 UI 本身可以实现，建议使用 Compose `Canvas` 自绘。
- “7天 / 30天趋势”不能指望首次安装后立即拥有完整历史。
- 内存趋势、温度趋势、建议洞察必须由本应用持续采样并本地存储。
- 应用使用时长分布可部分依赖 `UsageStatsManager`，但仍建议落库做自己的聚合快照，避免每次全量扫描。

## 3. 模块拆解与可行性

### 3.1 时间维度切换

设计稿内容：

- 今日
- 7天
- 30天

实现建议：

- UI 做成统一 `SegmentedTabs`
- 实际数据层区分：
  - `Today`: 小时级或较高频聚合
  - `7 Days`: 日级聚合
  - `30 Days`: 日级聚合 + 稀疏展示

可行性评估：

- `A`

### 3.2 内存趋势

实现建议：

- 每次采样记录：
  - timestamp
  - availMem
  - totalMem
  - lowMemory
- 页面查询后按日/时段聚合成折线

可行性评估：

- `B`

限制：

- 无法回填安装前历史
- 高精度实时波动没有必要，建议做区间平均值或抽样点

### 3.3 温度变化

实现建议：

- 存储电池温度
- API 29+ 同步存储 thermal status
- 图中“最高 40.6°C”使用区间最大值

可行性评估：

- `B/C`

限制：

- 第三方应用一般只能稳定拿到电池温度，不等于整机温度
- 不同机型对热状态支持不一致
- 因此这一模块更适合命名为“温度与热状态变化”

### 3.4 流量统计

设计稿内容：

- Wi-Fi + 移动数据堆叠柱状图

实现建议：

- 按日汇总 `NetworkStatsManager` 统计
- 保存日快照，减少反复全量扫描系统历史

可行性评估：

- `B`

限制：

- 依赖使用情况访问权限
- 某些网络细节统计并不适合承诺“与系统设置完全一致”

### 3.5 使用时长分布

设计稿内容：

- 环图
- Top 应用列表
- 百分比

实现建议：

- 通过 `UsageStatsManager` 汇总指定日期区间使用时长
- 仅取 Top N 应用，其余合并为 “其他”
- 环图使用 `Canvas` 绘制

可行性评估：

- `B`

限制：

- 仍依赖使用情况访问权限
- 首次授权后当天历史通常可读，但跨天展示仍建议配合本地快照稳定化

### 3.6 本周洞察

设计稿内容：

- 以一句话总结趋势

实现建议：

- 不做大模型依赖
- 通过规则模板生成，例如：
  - 夜间充电时温度升高
  - 内存持续偏低
  - 某应用使用时长显著上升
  - 移动数据消耗异常

可行性评估：

- `A/B`

限制：

- 结论必须基于可解释规则，避免“看起来很智能但没有数据依据”

## 4. 采样与存储方案

### 4.1 建议表结构

```text
device_snapshot
- id
- sampled_at
- available_memory_bytes
- total_memory_bytes
- battery_level
- battery_temperature_celsius
- thermal_status
- storage_free_bytes
- storage_total_bytes
- wifi_rx_bytes
- wifi_tx_bytes
- mobile_rx_bytes
- mobile_tx_bytes

app_usage_snapshot
- id
- sampled_date
- package_name
- foreground_time_millis
- launch_count_estimate
- mobile_bytes
- wifi_bytes
- storage_bytes
- last_time_used
```

### 4.2 采样节奏

建议首版：

- 冷启动采样
- 回到前台采样
- `WorkManager` 周期采样

说明：

- 不建议做高频后台常驻服务
- 趋势图优先服务“日级观察”，不是做性能监控器

## 5. 图表实现方式

建议全部自绘：

- `TmLineChart`
- `TmBarChart`
- `TmDonutChart`

原因：

- 图形类型简单
- 颜色、圆角、渐变、标注风格能完全贴合设计稿
- 避免第三方图表库引入额外学习与维护成本

## 6. 需要调整的预期

分析页有三个必须提前告知的方向问题：

- “7天/30天”不是系统帮我们保存好的全量历史，安装初期只能逐步积累。
- “温度变化”应明确成“电池温度/热状态趋势”，不能包装成 OEM 级硬件温度监控。
- “本周洞察”应是规则驱动建议，不要承诺 AI 诊断。

## 7. 参考资料

- [Compose Graphics / Canvas](https://developer.android.com/develop/ui/compose/graphics/draw/overview)
- [UsageStatsManager](https://developer.android.com/reference/android/app/usage/UsageStatsManager)
- [NetworkStatsManager](https://developer.android.com/reference/android/app/usage/NetworkStatsManager)
- [PowerManager Thermal APIs](https://developer.android.com/reference/android/os/PowerManager.html)
- [WorkManager](https://developer.android.com/jetpack/androidx/releases/work)
- [Room](https://developer.android.com/jetpack/androidx/releases/room)
