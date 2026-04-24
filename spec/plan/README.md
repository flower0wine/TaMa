# TaskManager 实施方案总览

## 1. 目标与结论

根据 `spec/ui` 下的四张设计稿，这个项目更接近“设备状态与应用使用分析工具”，不是传统的任务管理类应用。当前工程还是空白 Android App，适合直接按新架构搭建，不需要考虑旧代码兼容。

整体结论：

- UI 设计大部分可以实现，并且更适合使用 `Jetpack Compose + Material 3` 落地。
- 设备总览、应用列表、权限状态、系统设置跳转，这些能力可稳定实现。
- 应用使用时长、前台切换、最近活跃、应用级存储占用、设备级流量统计可以实现，但依赖 `PACKAGE_USAGE_STATS` 授权，且部分数据精度受系统聚合策略影响。
- 温度、热状态、历史趋势、智能建议只能“条件实现”或“近似实现”，不能承诺像 OEM 系统管家那样拿到完整底层数据。
- 设计稿中的趋势页如果要求展示“过去 7 天/30 天”的真实历史曲线，必须由本应用自行持续采样和落库；首次安装后无法回填完整历史。

## 2. 推荐技术方向

### 2.1 UI 层

- 使用 `Jetpack Compose`
- 使用 `Material 3`
- 使用单 Activity + `Navigation Compose`
- 图表优先用 `Canvas` 自绘，不引入第三方图表库

这样做的原因：

- 设计稿包含大量圆角卡片、胶囊筛选、底部导航、状态色数字、趋势图和可复用信息块，Compose 更容易模块化复用。
- 官方文档已经明确 Compose BOM 可统一管理 Compose 依赖版本，适合从零搭建项目。
- 图表仅为折线图、柱状图、环形图，复杂度可控，自绘比接入第三方图表库更轻、更统一，也避免依赖维护风险。

## 3. 推荐分层

建议按下面结构组织：

```text
app/src/main/java/com/flowerwine/taskmanager
  core/
    designsystem/
    ui/
    model/
    util/
  data/
    source/
      local/
      system/
    repository/
  domain/
    usecase/
  feature/
    overview/
    analysis/
    apps/
    tools/
  navigation/
```

说明：

- `core/designsystem`：主题、色板、圆角、阴影、通用卡片、标签、底部导航、空态。
- `data/system`：所有 Android System Service 读取逻辑集中管理，避免散落在页面里。
- `data/local`：Room / DataStore。
- `feature/*`：按页面拆分 UI、ViewModel、状态模型。
- `domain/usecase`：聚合多个 system source 与 repository 的业务逻辑。

## 4. 数据层建议

### 4.1 持久化

- `Room`：保存趋势采样数据、应用聚合统计快照、建议记录。
- `DataStore`：保存筛选条件、排序方式、用户是否完成权限引导、最近一次采样时间。

选择依据：

- 官方文档建议：复杂结构化数据使用 Room，小型配置数据使用 DataStore。
- 趋势页面需要按日期、维度、应用包名做聚合查询，Room 更合适。

### 4.2 后台采样

- 使用 `WorkManager` 执行周期采样
- 建议最小可行版本先做：
  - 应用启动时即时采样一次
  - 前台恢复时补采样一次
  - 周期任务做低频补采样

原因：

- 趋势数据并非系统统一提供给第三方 App。
- `WorkManager` 适合“可延迟但需要可靠执行”的采样任务。
- 不能依赖高频后台实时轮询，否则容易被系统限制，也不利于续航。

## 5. 主题与组件复用要求

应统一建设一套设计系统，而不是在页面中硬编码颜色和尺寸。

建议最少抽象出这些组件：

- `TmScaffold`
- `TmTopHeader`
- `TmSegmentedTabs`
- `TmMetricCard`
- `TmSectionCard`
- `TmAppListItem`
- `TmStatusChip`
- `TmActionButtonRow`
- `TmInsightBanner`
- `TmPermissionRow`
- `TmLineChart`
- `TmBarChart`
- `TmDonutChart`

颜色策略：

- 使用 `MaterialTheme.colorScheme` 扩展语义色
- 在 `designsystem` 中定义扩展色，例如 `memoryBlue`、`thermalOrange`、`storagePurple`、`networkGreen`
- 禁止页面直接写裸十六进制颜色

## 6. 可行性分级

建议在后续开发中统一使用以下标记：

- `A`：可直接实现
- `B`：可实现，但依赖授权、后台采样或设备支持
- `C`：只能部分实现，需要调整预期或 UI 文案

当前总评：

- 总览页：`A/B`
- 分析页：`B/C`
- 应用页：`B`
- 工具页：`A/B`

## 7. 推荐依赖方向

以下版本均来自我本轮已核查的官方 Android Developers 发布页，适合作为 2026-04-23 的规划基线：

- Compose BOM：`androidx.compose:compose-bom:2026.02.01`
- Activity Compose：`androidx.activity:activity-compose:1.12.0`
- Navigation Compose：`androidx.navigation:navigation-compose:2.9.5`
- Room：`androidx.room:room-*:2.7.2`
- DataStore：`androidx.datastore:datastore-preferences:1.2.1`
- WorkManager：`androidx.work:work-runtime-ktx:2.11.2`

说明：

- 这里只给出规划建议，不在本次任务里直接修改 Gradle。
- 如果后续要正式导入依赖，仍建议在改动前再核对一次官方发布页，避免版本再次变化。

## 8. 方向性提醒

有两个方向问题需要提前确认：

- 如果你希望这个 App 达到“系统管家”级别的数据完整度，第三方应用无法完全做到，尤其是温度、热状态来源、历史回溯、跨应用精细网络统计都会受限。
- 如果你接受“第三方工具型 App”的边界，那么这套设计稿可以保留大部分视觉结构，只需要把个别文案从“确定性结论”调整为“估算/趋势/建议”。

## 9. 参考资料

- [Compose BOM](https://developer.android.com/jetpack/compose/bom)
- [Compose Graphics / Canvas](https://developer.android.com/develop/ui/compose/graphics/draw/overview)
- [Activity Compose Release Notes](https://developer.android.com/jetpack/androidx/releases/activity)
- [Navigation Release Notes](https://developer.android.com/jetpack/androidx/releases/navigation)
- [Room Release Notes](https://developer.android.com/jetpack/androidx/releases/room)
- [DataStore Guide](https://developer.android.com/topic/libraries/architecture/datastore)
- [DataStore Release Notes](https://developer.android.com/jetpack/androidx/releases/datastore)
- [WorkManager Release Notes](https://developer.android.com/jetpack/androidx/releases/work)
