# Android 混合 UI 技术方案（Compose + View）

## 1. 目标

在当前 `animegen-android` 项目中引入一页可实际使用的原生 View 页面，而不是纯 Demo：

- 页面名称：`作品批量操作中心（View版）`
- 页面价值：支持作品列表多选、批量复制作品 ID、快速刷新、排序切换
- 入口位置：设置页

该页面用于承接“高密度列表 + 批量交互”场景，同时保留主流程使用 Compose 的开发效率。

## 2. 方案结论

采用 **混合 UI 架构**：

- 主业务导航、常规表单和详情页：继续使用 Compose
- 高密度批量列表操作页：使用原生 View + XML（`RecyclerView`）

核心原则是“按场景选技术”，不是“一刀切”。

## 3. 为什么这个页面用 View

`作品批量操作中心` 具备以下特点：

- 长列表，频繁勾选与取消勾选
- 局部状态更新（仅选中态变化）远多于整体刷新
- 需要稳定的列表复用与细粒度刷新控制

在这类场景，原生 View 的优势更直接：

- `RecyclerView + DiffUtil + payload` 可以精确局部刷新
- 稳定 ID、回收池、item 动画策略成熟
- 对“批量操作工具页”这类工程化页面，XML 结构直观，调优路径清晰

## 4. 为什么其他页面继续 Compose

当前项目已有大量 Compose 页面（创作、任务、社区、个人中心等），其优势明显：

- 状态驱动 UI，一致性更好
- 新页面开发速度快
- 动态主题、组合式复用更自然

因此保留 Compose 作为主流开发路径，避免无意义迁移。

## 5. 分层边界（建议长期遵守）

- **Compose 适用**：新业务流程页、表单页、详情页、轻中量列表
- **View 适用**：超长列表、复杂 item 复用、批量编辑工具页、对局部刷新控制要求很高的页面
- **禁止**：同一页面核心区域频繁在 Compose/View 间来回嵌套，避免维护复杂度上升

## 6. 本次落地内容

### 6.1 入口

- 设置页新增入口按钮：`打开作品批量操作中心（View版）`

### 6.2 新页面（View）

- `NativeWorkCenterActivity`
- XML 页面布局 + XML 列表 item
- 使用真实数据源：`worksRepository.listWorks(limit = 60)`
- 支持：
  - 刷新
  - ID 排序切换
  - 多选
  - 批量复制选中作品 ID 到剪贴板

### 6.3 关键技术点

- `RecyclerView` + `ListAdapter`
- `DiffUtil.ItemCallback`
- `getChangePayload` + `onBindViewHolder(payloads)` 进行选中态局部刷新
- `setHasStableIds(true)` + `getItemId()` 提升列表稳定性

## 7. 影响评估

- 对现有 Compose 主流程无破坏
- 只新增一个 Activity 和少量资源
- 可作为后续“批量管理/审核工具页”模板复用

## 8. 文件清单

- `animegen-android/app/src/main/java/com/animegen/app/ui/screen/nativeview/NativeWorkCenterActivity.kt`
- `animegen-android/app/src/main/res/layout/activity_native_work_center.xml`
- `animegen-android/app/src/main/res/layout/item_native_work_row.xml`
- `animegen-android/app/src/main/res/drawable/bg_native_row.xml`
- `animegen-android/app/src/main/res/drawable/bg_native_status_badge.xml`
- `animegen-android/app/src/main/java/com/animegen/app/ui/screen/settings/SettingsScreen.kt`
- `animegen-android/app/src/main/AndroidManifest.xml`
- `animegen-android/app/src/main/res/values/strings.xml`
- `animegen-android/app/build.gradle.kts`

## 9. 后续演进建议

1. 在该页增加“状态筛选（SUCCEEDED/FAILED/RUNNING）”和“分页加载”。
2. 批量操作从“复制 ID”升级为“批量发布/批量重试”等真实业务动作。
3. 如果后续 Compose 列表性能与交互可达标，可再评估逐步统一；否则保持混合架构。
