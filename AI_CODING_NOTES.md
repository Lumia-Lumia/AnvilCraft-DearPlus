# AI_CODING_NOTES

铁砧工艺（AnvilCraft）附属 mod **AnvilCraft: DearPlus** 的完整开发记录与交接说明（含全部用户需求）。

- **环境**：Minecraft 1.21.1 + NeoForge，分支 `releases/1.21.1/1.6`
- **参考**：https://github.com/Anvil-Dev/AnvilCraft
- **用途**：为 AI/开发者提供跨会话的项目上下文、完整需求、功能清单、技术要点与当前状态。

---

## 一、会话历史总览

| 会话 | 时间 | 内容 | 结果 |
|---|---|---|---|
| 1 `2bdd1d5c` | 2026-07-26 | "Autumnium Alloy" 名称显示不正确 | 未解决（模型/权限故障） |
| 2 `3eb0b138` | 2026-07-26 ~ 07-31 | 基础物品（合金/合金块）+ 重锻面板（锻星砧接口） | 完成，结尾待复测重锻 |
| 3 `b0a3e4b9` | 2026-08-06 ~ 08-12 | 主开发：3 物品 + 5 词条 + 全套机制 | 完成，结尾遗留 3 个九环刀问题 |
| 4 `d6467b96` | 2026-08-12 ~ 08-13 | 解决遗留问题、逐鹿免费用、命名重构、3D 模型 | 基本完成；结尾遗漏 3eb0b138 总结 |
| 5 | 2026-08-13 ~ 08-15 | 版本更新、共振器/大环刀收尾、逐鹿设定重构、代码审查 | 完成，遗留审查报告 16高+17中 待修复 |
| 6 | 2026-08-15 ~ 08-16 | 代码审查修复（22 项）、攻击 6/7/8/12、共振器耐久 254/攻击 6、飘升机粒子复用本体+飞行同步、**完整重命名 AnvilCraft: DearPlus（mod id→anvilcraft_dearplus、包名/命名空间/手册目录全改）** | 基本完成 |
| 7（当前） | 2026-08-16 | 会话记录整理：历史会话 jsonl/附属目录/memory 从旧路径 `~/.claude/projects/...-CelestialReforge` 迁移至 `...-DearPlus` 并删除旧目录 | 完成 |
| 8 | 2026-08-16 | 更新铁砧工艺至 `1.6.0+snapshot.2156`（AnvilLib 同步 `2.0.0+snapshot.506`），修复 `ModRegistries` API 变更导致的启动崩溃 | 完成 |

---

## 二、会话 1（`2bdd1d5c`）详情

- **需求**：注册物品 "Autumnium Alloy"（秋枫合金）在游戏中名称显示不正确，帮忙找出问题。
- **过程**：因模型配置错误（`model_not_found`，deepseek-v4-flash 不可用）和工具权限反复被拒，始终无法读取代码。
- **结论**：此类问题通常是**语言文件（本地化 key）缺失或与注册名不匹配**（后续会话中已隐式修复）。

---

## 三、会话 2（`3eb0b138`）详情 —— 基础物品与重锻面板

（2026-07-26 ~ 07-31，位于会话 1 与主开发之间）此会话实现了**秋枫合金 / 秋枫合金块**的注册与配方，以及完整的**重锻面板**（锻星砧接口方块）。用户全部需求记录如下。

### 3.1 秋枫合金与合金块（07-26 ~ 07-27）

- 修复 Autumnium Alloy 名称不显示（承接会话 1）：
  - 根因 1：mod ID 由 `anvilcraft_addon_template` 重命名为 `anvilcraft_dearplus` 后未重新运行数据生成，`src/generated/resources` 仍是旧命名空间 → 运行 `./gradlew runData`
  - 根因 2：缺少物品纹理 `textures/item/autumnium_alloy.png`，`runData` 报 `Texture ... does not exist` → 先用占位纹理复制
- `example_block` → `autumnium_alloy_block`：完成注册、方块/物品模型、方块状态、战利品表
- 配方：
  - 有序：9 × 合金 → 1 × 合金块（用 `ShapedRecipeBuilder`；不用 `threeByThreePacker`，参数顺序易错）
  - 无序：1 × 合金块 → 9 × 合金
  - 无序：1 铜锭 + 1 陶瓦 + 1 ×（`is_autumnium_component` 标签）→ 1 合金；成分含白桦树叶/樱花树叶/鹿角珊瑚/鹿角珊瑚扇
- 新建标签 `is_autumnium_component`（`ModTags` + `AddonTagHandler` 数据生成）
- 坑：`.save(prov, "name")` 不指定命名空间默认存到 `minecraft`，需用 `ResourceLocation` 显式指定本模组命名空间

### 3.2 重锻面板 —— 完整需求与实现

**设计定位**：锻星砧（CFA）的接口方块，仿物流接口；仅可紧贴锻星砧底边水平放置，方向固定背对锻星砧；右键打开 GUI 配置筛选条件；收到红石信号后按筛选条件自动重锻天体。

**07-27 07:41 初版需求（六维筛选）：**
- 右键打开 GUI 配置筛选条件；收到红石信号开始自动锻造：天体满足条件 **或** 无法锻造 → 向不紧贴锻星砧的五个面释放强度 15 红石且不重锻；反之天体不满足条件且可锻造 → 执行重锻，不发射红石
- 筛选条件：磁场（无/几乎没有/非常弱/弱/中等/强/非常强）、转速（非常慢/慢/中等/快/非常快）、温度（极寒/寒冷/温和/炎热/焦土）、大气层（有/无）、流体覆盖率（无/低/中等/高）、天体资源（生物/祭品/废土 各有无）
- 每条条件可勾选参与/不参与筛选，默认不参与

**实现架构（新增文件）：**
- `block/ReforgingPanelBlock.java`、`block/entity/ReforgingPanelBlockEntity.java`（核心 tick / 筛选 / 重锻）
- `inventory/ReforgingFilter.java`（筛选枚举）、`inventory/ReforgingFilterData.java`（NBT 持久化）
- `inventory/ReforgingPanelMenu.java`（ContainerData 同步 + `clickMenuButton` 通信）
- `client/gui/screen/ReforgingPanelScreen.java`（GUI）
- `init/ModBlockEntities.java`、`init/ModMenuTypes.java`；`CelestialForgingAnvilBlockEntityMixin` 暴露 CFA 私有字段（后续被公开 API 替代移除）

**关键 bug 修复：**
- 服务端 `createMenu` 传了 `null` MenuType → 改用已注册的 `ModMenuTypes.REFORGING_PANEL.get()`
- GUI 打开即 `StackOverflowError`：`renderBg()` 里调 `renderBackground()`，而 AnvilCraft 的 `AbstractContainerScreenMixin` 让 `renderBackground` 又调 `renderBg` → 死循环；删除 renderBg 中的调用即可
- `reforging_panel.json` 资源重复（数据生成 + 手动）→ `processResources` 报 duplicate；改由 Registrum 不生成该方块状态/物品模型，只用手动文件
- Mixin 接口方法名与 CFA 冲突 → 改名；接口不能放 mixin 包（外部不可引用）→ 移到普通包

**筛选条件迭代（最终为 3 项）：**
- 07-27：GUI 五条（磁场/转速/温度/流体覆盖率/行星特殊资源=有/无）；磁场去除"非常弱"
- 07-28：删除温度和流体覆盖率筛选；行星资源先拆为生物资源（有/无）+ 文明资源（有/无），再合并回"行星特殊资源"，选项：**任意资源 / 生物资源 / 文明资源 / 无**
  - 生物资源 = 行星含 生物资源 或 生物流体资源；文明资源 = 含 祭品资源 或 废土资源；任意 = 生物或文明任一满足；无 = 两者都不满足
- 最终 GUI 三项：**天体磁场 / 天体转速 / 行星特殊资源**；英文显示 Mag. Field / Rot. Speed / Planetary

**GUI 交互需求（全部实现）：**
- 显示自然语言而非 id；勾选框表示参与筛选（默认不勾选，左键切换）；循环按钮左键正向/右键反向/滚轮切换
- 配置持久化：点击即自动保存到 BE NBT，关闭界面（Esc/E）即保存；无保存按钮；保存后重开显示上次状态
- bug：第一次点击无反应 → 点击时乐观更新本地数据
- 布局：标题在复选框前、值文本在轮换按钮上；元素位于背景内且互不重叠、按钮靠右、标题白字；勾选框 16×16、值文本按钮 64×30（后 64×16）；标题与值文本高度对齐；值文本上移 1px；标题居中；两侧留白
- 按钮材质上下分半，**仅鼠标悬停**显示下半部分（聚焦不切换）
- GUI 背景：`anvilcraft:textures/gui/backgrounds.png` 从 (176,232) 起的 176×76 区域，居中；与时空超算同款配置
- 中文名"天体磁场/天体转速"；英文缩写 Mag. Field / Rot. Speed / Planetary；尝试减小英文字号 → 撤回（位图字体无法缩小）

**外观 / 模型迭代：**
- 模型：锻星砧接口占位符 → 物流接口 → 自定义 `reforging_panel.json`（多次更新，用本模组命名空间）；碰撞箱匹配物流接口形状
- 方块状态按 `powered` 切换模型；**active 模型**用于"被激活"与"重锻过程中"
- 导线材质：`#4` 面加 `tintindex:0` + `BlockColor` 按 `RedStoneWireBlock.getColorForPower()` 染色；后取消随充能染色，始终原色
- 物品形态不染色（与红石导线物品一致）；后改为与红色水泥色一致 `0xB3312C`
- 粒子：改用不含导线的 `celestial_forging_anvil_interface` 纹理，避免被方块染色
- 锻星砧被破坏时面板**不**连锁破坏

**红石逻辑迭代（重点难点，最终形态）：**
- 信号方向反复调试：`getSignal` 的 `direction` 参数方向约定易搞反；`FACING` 为远离锻星砧方向，目标方向 = `FACING.getOpposite()`
- CFA 用 `getBestNeighborSignal()` 读**强充能**（`getDirectSignal`），只发弱充能 CFA 无响应 → 曾让 `getDirectSignal` 在 FACING 方向返回 3，后删除（继承默认 0），并在 `setEmitting()` 主动调 CFA `markRedstoneSignalDirty()`
- 区分两个状态：**POWERED**（方块状态，模型激活 / 重锻中）与 **EMITTING**（天体匹配筛选，才真正发信号）；重锻过程中 POWERED=true 但 EMITTING=false，不发射
- 最终信号：**面板激活（水平侧面收到信号）且天体匹配筛选条件**时，向上、下、FACING 方向发出强度 3 的弱充能信号
- 面板只能通过水平四个侧面（强/弱充能均可）激活，上下两面输入不激活 → 自定义 `hasHorizontalSignal()`，对每个水平邻居同时查 `dir` 与 `dir.getOpposite()` 的 `hasSignal` 和 `getDirectSignal`
- CFA 锁定/解锁：面板激活且天体不符合 → 解锁锻星砧；激活且符合 → 锁定；筛选条件不合理 → 解锁

**配方（07-28）：**
- 3×3：QJQ / CSG / QHQ → 2 重锻面板（Q=秋枫合金块，J=结构扫描仪，C=磁盘，S=时空超算，G=过滤器，H=红石导线）；07-30 改为 Q=秋枫合金

**CI / 工程：**
- CI 崩溃 `NoClassDefFoundError: dev/anvilcraft/lib/v2/config/ConfigManager` = AnvilLib 版本不匹配 → `.github/workflows` 的 `extra-mods` 更新为 anvilcraft 1.6.0+snapshot.2099
- 分支改名 `releases/1.21.1/1.6`

**文档（ageratum 手册）：**
- 中文：`assets/anvilcraft/ageratum/zh_cn/173_dearplus/`（`index.md` + `000_reforging_panel.md` 介绍重锻面板）
- 英文：同结构 `en_us/` 版本

**语言文件与 tooltip：**
- 为合金 / 合金块 / 重锻面板添加中文与 tooltip：
  - 秋枫合金：一块充满了秋之魔力的复合材料
  - 秋枫合金块：一大块充满了秋之魔力的复合材料
  - 重锻面板：激活后能自动重锻星辰，可使用GUI调整筛选项
- tooltip 实现：`@EventBusSubscriber` 监听 `ItemTooltipEvent`（后参考 AnvilCraft PR 改为更简方式）
- 物品组中文名："铁砧工艺：鹿+"
- 清理：删除未使用条目、整理排序；确认并删除无用的生成版 `en_us.json` / `en_ud.json`

**07-31 代码审查清理：**
- 删除无用代码（`getAll()`、`isEmitting()` getter 等）
- 性能优化：`tick()` 空闲早退、`setEmitting()` 减少重复 CFA 查找、`containerTick` 仅值变化时更新、`hasHorizontalSignal` 合并判断
- **Mixin 大清理**：AnvilCraft 1.6.0 已有公开 API，`CelestialForgingAnvilBlockEntityMixin` 大部分可移除 → 改用公开 API，删除 Mixin 文件与 `mixins.json` 注册
- 重锻功能失效 → 加 `reforgeCooldown`（250 tick）防重复触发；曾试加 `searchGuardTicks` → 用户要求撤回

**会话结尾遗留**：撤回 searchGuardTicks 修改后，用户要求重新测试重锻功能（本会话在此处结束，未复测结论）。

---

## 四、会话 3（`b0a3e4b9`）详情 —— 主开发

### 3.1 初始完整需求规格（08-06 00:28）

参考铁砧工艺代码，为附属添加以下内容：

**新增 5 词条（类似无情的词条）：**
- **偏安**：无法通过正常途径（铁砧、附魔台等）附魔，无法被砂轮祛魔。使所有附魔（包括诅咒）全部失效。
- **逐鹿**：附魔无需消耗经验。必定触发一次"强运"。
- **破竹**：效果等同同等级的锋利魔咒，不会被偏安禁用。
- **枭首**：效果等同同等级的斩首魔咒，不会被偏安禁用。
- **豪夺**：效果等同同等级的抢夺魔咒，不会被偏安禁用。

**秋枫共振器：**
- 配方：秋枫合金/合金块/避雷针（3x3 网格）
- 偏安词条；耐久 254、挖掘等级铁；只有共振器形态（不可 alt 切换为工具形态、可共振挖掘）；无法铁砧修复；工作台+合金块（无序）→满耐久

**秋枫飘升机：**
- 配方：幻翼膜/合金块/幻翼膜 ×2 + 锡压力板/皮革外套/锡压力板（后改为羽毛+铜压力板版）
- 偏安词条；耐久 432、护甲+4；创造飞行（同飘升机背包）；飞行每秒-1 耐久（类似鞘翅）；耐久到 1 失效+损毁纹理；损耗在 1 停止（不会损坏消失）；无法铁砧修复；工作台+合金块修复

**秋枫大环刀（四档）：**
- **三环刀**：配方（合金块/皮革/金属粒/木棍）；偏安；金属粒=铁→破竹、铜→枭首、金→豪夺，每粒 1 级；耐久480/攻6/攻速1.6；**真实横扫**（攻击未命中也触发横扫）；无法铁砧/两把相同/砂轮修复；工作台+合金块修复（保留词条）
- **五环刀**：三环+2金属粒 **物品冲压**；偏安；耐久540/攻7/攻速1.8；在原先基础上累加词条
- **七环刀**：五环+2金属粒 **高温熔炼**；偏安；耐久800/攻8/攻速1.8
- **九环刀**：七环+2金属粒 **二合一锻造**（余烬锻造台）；**逐鹿**（无偏安）；耐久1999/攻12/攻速2；两把九环刀可铁砧合并，词条=两把之和减二（**后改为取最小值**）

### 3.2 后续迭代需求（按时间，均已实现）

**08-06**
- 铜粒使用铁砧工艺自带的铜粒，不要新注册。

**08-10**
- 修改译名（英文）：Autumnium Resonator / Ionocraft / 3~9-Ringed Broadsword（后加 Autumnium）；逐鹿=Vying、破竹=Chopper、枭首=Decapitator、豪夺=Dispossessor
- 秋枫共振器无法按 alt 打开轮盘（预期：无法打开）
- 检查"无情/重铸/残暴/强运/永恒"实现，对比新词条
- 词条 tooltip 位置：从底部移到名称下方第一行
- 去除"词条·"前缀，像铁砧工艺一样加冒号和描述
- 临时外观：共振器用余烬共振器、飘升机用飘升机背包、刀用余烬金属剑
- 飘升机用背包模型（多次迭代到穿戴独立模型 `getHumanoidArmorModel`）
- 清理无用冗余代码
- 飘升机配方最终定为：羽毛/合金块/羽毛 ×2 + 铜压力板/皮革外套/铜压力板
- 注册 id 最终：`autumnium_resonator`、`autumnium_ionocraft`、`ringed_autumnium_broadsword_3/5/7/9`
- 九环刀二合一锻造无法合成 → 修槽位（模板槽/材料槽/输入槽×2）
- 升级词条不继承（白板）→ 修 `AutumniumAffixMergeOutcome`（改为直接生成带词条实体）
- 两个相同金属粒应升 2 级
- 词条升级只与消耗的金属粒相关（原始量-消耗后剩余）
- 多把刀同时冲压词条异常 → 一次只加工一把（`matches` 限制恰好 1 把）
- 多把刀铁砧砸下崩溃 → 刀数检查移到 `super.matches` 之前
- 豪夺掉落超堆叠上限崩溃（`[1;99]: 3811`）→ `growDrop` 拆分
- 九环刀铁砧合并：取两者最小值（无词条=0）
- 偏安物品仍可铁砧附魔 → `AnvilMenuMixin` 硬阻止

**08-12**
- 金属粒只含铁金铜 → 创建标签 `is_broadsword_ring_component`
- 飘升机贴图更新（物品贴图 + 模型贴图）
- 查看飘升机背包粒子实现 → 为飘升机加粒子（羽毛色 白至亮灰）→ 形状同背包只改颜色 → 颜色修正（蓝色→白色）→ anvilon_space 纹理降饱和提亮 → 再降饱和 → 每喷口减半（**会话 6 起：飘升机粒子已复用本体 `ModParticles.IONOCRAFT_BACKPACK_EXHAUST`，删除自建粒子类型/类/贴图**）
- 真实横扫功能未实现 → 实现并迭代（伤害=直击、范围扩大、取消距离判定、水平±2.5垂直±1、挥空面前范围、中心随视线、命中横扫统一大范围）
- 检查代码报告功能
- 词条注册名改为英文译名（tranquil/vying/chopper/decapitator/dispossessor）
- 标签 `maple_ring_blades` 统一为英文译名 → `ringed_autumnium_broadswords`
- 删除 inventoryTick 防御性清理附魔
- .java 文件名统一为注册名；`maple_*` 全改 `Autumnium_`
- 偏安附魔无效化机制（讲解）
- 浮霜工具"无情"实现（讲解）
- inventoryTick vs PlayerTick 性能（讲解）
- 以 PlayerTick 将偏安附魔转移到 `TRANQUIL_ENCHANTMENTS`，像无情一样显示；七环→九环移回 ENCHANTMENTS
- tooltip：TRANQUIL_ENCHANTMENTS 显示最上面、颜色 `#5d241f`；词条颜色：偏安`#481318`（后改`#79422c`）、破竹`#d7af91`、枭首`#b78766`、豪夺`#965f3b`、逐鹿`#fa9554`
- 真横扫只在攻击时挥刀（打开 GUI/丢物品/骑乘不挥刀）→ 自定义包 `AutumniumSwingPacket`
- 创造模式按 q 丢刀消失 = 原版行为，无需改
- 总结秋枫工具功能
- 逐鹿附魔免经验功能未实现 → 修（`EnchantmentMenuMixin` 免经验等级检查）
- **结尾遗留 3 问题**（09:25）：九环刀无法砂轮祛魔、无法附魔台附魔、铁砧附魔消耗经验（因上下文超限未完成）

---

## 五、会话 4（`d6467b96`）详情 —— 全部需求

1. 继续完成九环刀 3 问题：砂轮祛魔、附魔台附魔、铁砧免经验
2. 现状反馈：附魔台"附魔能力受限"（根因=刀不在 `#minecraft:swords`）、铁砧创造显示费用/生存无法进行 → 修复
3. 铁砧附魔时消耗等级显示为 0（而非显示但不扣）
4. 九环刀铁砧合并费用应为 0；**bug：合并无法合并魔咒** → 修复（按原版规则合并魔咒）
5. 附魔台免等级要求 + 附魔后魔咒不刷新 → 修复
6. 附魔台按钮亮起只与青金石相关 → 客户端 mixin
7. 确认"附魔免经验+必定强运"是逐鹿词条功能（组件驱动）而非九环刀属性
8. 所有文件错误名称改为正确名（词条名 + 物品名）→ 全面重构
9. 应用正式模型（反复迭代）：3D 模型转换、方向旋转 90°、贴图修复、负方块保留、GUI 调优
10. 阅读重戟和共振器渲染方式，应用类似 GUI 调整
11. GUI：再放大、向左下偏移、验证方向（+y=上）、最终微调

---

## 六、会话 5（当前）详情 —— 收尾与审查

（2026-08-13 ~ 08-15）本会话承接 d6467b96，完成版本更新、共振器/大环刀收尾、逐鹿设定重构、代码审查。用户需求记录如下。

### 6.1 版本与基础
- 铁砧工艺更新至 `1.6.0+snapshot.2144`，AnvilLib 同步 `2.0.0+snapshot.500`（匹配内嵌版本）；workflow extra-mods / 发布依赖 / mods.toml versionRange `[1.6.0,)` 已同步
- 补充 3eb0b138 会话（重锻面板 + 基础物品）至本文档（见三）
- 资源清理：删除 `src/main` 与生成重复的 alloy 模型/blockstates；修复 `build.gradle` 重复 `srcDir 'src/generated/resources'`（消除 processResources 覆盖警告）

### 6.2 秋枫共振器
- 耐久 250 → **254**（AddonTiers）；共振挖掘特性保留（继承 ResonatorItem）
- **挖掘等级铁**：覆写 `isCorrectToolForDrops`（ResonatorItem 未覆写会误挖黑曜石/远古残骸）
- **正式齿轮贴图** `textures/item/autumnium_gear.png`（32×32），模型覆盖 `textures."1"` 引用，结构保持共振器统一模型
- 删除共振器通用 tooltip（覆写 appendHoverText 空）
- 配方修正：`AB / C / A`（合金、合金块、空 / 空、避雷针、空 / 空、合金、空）

### 6.3 秋枫大环刀
- **物品栏渲染裁切**：注册 `ItemSlotClipping`（FMLClientSetupEvent，避免 unbound）；GUI display 最终 `rotation [0,20,0], translation [-2.05,-1.9,0], scale 0.7`（公共 display 父模型）
- **亡灵伤害翻倍**（`AddonAffixEvents.onDamageUndead`，`EntityTypeTags.UNDEAD`）
- **真实横扫**：命中不受原版 flag2 限制；伤害 = `直击 × √(横扫之刃等级+1)` 向下取整
- **九环刀词条叠加**（二合一锻造：材料槽=超限多相合金 `anvilcraft:multiphase_transcendium`，输入槽=两把九环刀）：破竹/枭首/豪夺取和（上限 255）、必带永恒、附魔取两把最大（不考虑互斥）、逐鹿等级（相同+1/不同取最大，上限 10）
- **铁砧合并**：词条取最小、魔咒合并、REPAIR_COST 清零、逐鹿等级升级（同二合一规则）
- **逐鹿设定重构**：`VYING` 由 `Unit` 改 `Integer`（带等级，上限 10）；放弃"强运"；改为**稀有掉落概率提升**：`P → P+(1-P)×L/10`（P=0 时 P×L=0，不产生新掉落；10 级=必定）；已确认斩首附魔的 25%/5% 额外头颅池是 AnvilCraft 特性（非强运），逐鹿会提升斩首头颅基础池
- **金属粒升级词条**：只看种类（铁→破竹、铜→枭首、金→豪夺各 +1），不看堆叠数量
- **创造模式默认词条**：三环 111 / 五环 221 / 七环 322 / 九环 333
- **词条等级显示**：罗马数字（混合 ≤10 罗马 >10 阿拉伯 / 全罗马 / 全阿拉伯，config `affixNumberStyle`）；满级显示 `MAX`
- **tooltip 描述**：破竹"使刀造成更多伤害"、枭首"独立于斩首魔咒的斩首效果"、豪夺"使掉落物数量增加"、逐鹿"附魔无需消耗经验，随等级提升稀有掉落物的掉落概率"

### 6.4 飘升机 / 重锻面板
- 飘升机模型确认为正式（AnvilCraft 背包模型 + 自定义 64×32 贴图）；删除无用 `autumnium_ionocraft_layer_1.png`
- 重锻面板可被铁砧锤拆下：实现 `IHammerRemovable`

### 6.5 手册
- ageratum 手册整理完整：index + 000_alloy / 001_resonator / 002_ionocraft / 003_broadsword / 004_affixes / 005_reforging_panel（中英双语，重锻面板置于最后一章）

### 6.6 冲压产物位置
- 对齐原版：`AutumniumAffixMergeOutcome` 应用 `IRecipeResultOffsetBlock`（冲压平台 FACING×0.7 前方偏移）；`outputOffset = (0,-0.25,0)`（本项目 context.getPos() 基于 pos，原版基于 pos.below()，差一格）

### 6.7 代码审查（2026-08-15）
- 4 组并行 Agent + 全局补充，覆盖全部 70+ 文件
- 发现 **16 高 + 17 中 + 多项低**隐患，详见根目录 **`CODE_REVIEW_REPORT.md`**（含修复进度核对表 ⬜/🔧/✅）
- 高优先：H4 双重掷骰数学错误、H5 面板 locked 残留、H1 每 tick 背包扫描、H11 攻击力偏一、H10 词条上限不一致等

### 6.8 会话 6：代码审查修复（2026-08-15，已全部完成）

**高严重度修复（8）**
- **H4** 逐鹿稀有掉落概率：双重掷骰数学错误 → 条件概率改用 `L/10`，最终 `p+(1-p)×L/10`（`LootItemRandomChanceWithEnchantedBonusConditionVyingMixin`）
- **H5** 面板 CFA 锁定残留：`releaseCfaLock()`（已有巨构 `getActiveMegastructureIndex()≥0` 时不解锁）+ `ReforgingPanelBlock.onRemove` 清理 + deactivate/不匹配分支统一释放
- **H7** `AutumniumAffixMergeOutcome.Type` 注册到 `LibRegistries.OUTCOME_TYPE_REGISTRY`（新增 `AddonOutcomeTypes`）
- **H8** 冲压/熔炼配方 JEI 产物显示：覆写 `getResultItems()` 返回 target（执行仍走 outcome，避免双产物）
- **H10** 词条上限：`BladeAffixes.add()` clamp 至 `MAX_LEVEL=255`（新增常量）
- **H11** 大环刀攻击：modifier 公式修正（`attackDamage-1-tierBonus`），实际攻击 **6/7/8/12**（原偏 1 点）
- **H13** `ATTACK_SWING_TICKS` 只增不删 → 标记时顺带清理过期项
- **H14** ThreadLocal 布尔改 tick 标记（`ATTACKING`/`CLIENT_ATTACKING` 存 `gameTime`），异常中断后自动失效

**中严重度修复（9）**
- **M1** 破竹/亡灵×2 仅玩家武器近战（`DamageTypes.PLAYER_ATTACK`）触发
- **M2** 飘升机飞行 modifier：只有胸甲槽那件管理（行为对齐本体背包）
- **M3** 挥空横扫网络容差放宽至 3 tick（伤害口径确认不改：≥0.9 蓄力即满蓄力伤害）
- **M6** 枭首 PvP 掉落：PLAYER_HEAD 设 `ResolvableProfile`（带玩家皮肤）
- **M7** 亡灵×2 判定改 `instanceof`（与横扫口径统一，外部加标签物品不继承特性）
- **M11** 非大环刀玩家不再发送攻击挥空包（`LivingEntitySweepMixin` 加主手检查）
- **M12** 飘升机多人粒子：照搬本体背包机制（新增 S2C `AutumniumIonocraftFlyingPacket` + 客户端 `SYNCED_FLYING_PLAYERS`，服务端 `AutumniumIonocraftItem.playerTick` 监测广播）
- **M16** 面板 GUI `stillValid`：检查面板仍在 + 玩家 8 格距离（原恒 true）
- **M17** 词条上限魔法数字收敛（tooltip 引用 `MAX_LEVEL` 常量）；**共振器耐久 255→254**（避免与词条上限撞车）

**低严重度修复（5）**
- 中文补 5 条 config 翻译（`affix_number_style` 等）
- `ReforgingFilterData` 反序列化 clamp 值范围
- `AutumniumResonatorItem` 攻击提常量（`BASE_ATTACK_DAMAGE`）
- 删除 `AddonDatagen` 空 `gatherData` 死代码
- `RingedAutumniumBroadswordCraftingRecipe` 删冗余 pattern 字段

**决策不改（高 6 + 中 7）**：H1/H2/H3/H9/H15/H16、M4/M5/M8/M9/M13/M14/M15（含版本策略 `[1.6.0,)` 无界、H9 序列化冲突、M4 横扫误伤为预期）
**暂缓/搁置（H6/H12、M10）**：版本时序/兼容风险项

**会话 6 收尾数值修正**
- **共振器攻击 10→6**：`ResonatorItem.createAttributes` 实际攻击 = `1+attackDamage+tierBonus`，`BASE_ATTACK_DAMAGE` 7→3（`AutumniumResonatorItem`）
- **飘升机粒子复用本体**：改用 `ModParticles.IONOCRAFT_BACKPACK_EXHAUST`（本体 `anvilon_air` 贴图 + 本体 provider），删除自建 `AddonParticles` / `AutumniumIonocraftExhaustParticle` / 贴图 / JSON

### 6.9 会话 8：铁砧工艺更新至 1.6.0+snapshot.2156（2026-08-16）

**需求**：更新铁砧工艺至最新 `1.6.0+snapshot.2156`；附属与其不兼容崩溃（崩溃报告 `D:\MC\minecraft-exported-crash-info-2026-08-16T13-29-20.zip`）。

**崩溃根因**：AnvilCraft snapshot.2156 中 `ModRegistries` 类由 `dev.dubhe.anvilcraft.init.ModRegistries` **移到 `dev.dubhe.anvilcraft.init.registry.ModRegistries`**，且字段 `CUSTOM_DATA_TYPE_KEY` **重命名为 `CUSTOM_DATA_TYPE`**（类型由 `ResourceKey` 改为 `Registry` 实例）。附属 `ModCustomDataComponents.java` 仍引用旧包名旧字段 → `NoClassDefFoundError: dev/dubhe/anvilcraft/init/ModRegistries`。

**修复**：
- `gradle/libs.versions.toml`：anvilcraft `1.6.0+snapshot.2144` → `1.6.0+snapshot.2156`；anvillib `2.0.0+snapshot.500` → `2.0.0+snapshot.506`（与 AnvilCraft JarJar 内嵌版本一致，经 POM 确认）
- `ModCustomDataComponents.java`：import 改 `init.registry.ModRegistries`、`CUSTOM_DATA_TYPE_KEY` 改 `CUSTOM_DATA_TYPE`
- workflow：`ci.yml` / `pull_request.yml` 的 `extra-mods` 更新为 `anvilcraft:1.21.1-1.6.0+snapshot.2156`
- config section key 变更：anvillib 506 将 config section 命名从 `anvilcraft_dearplus` 改为点分 `anvilcraft.dearplus`（`en_us`/`en_ud` 由 runData 自动更新）；手工维护的 `zh_cn.json` 中两行 `configuration.section.anvilcraft.dearplus.common.toml*` key 需手动同步，否则中文 config 标题不显示

**验证**：`compileJava` / `runData` / `build` 全部通过；字节码确认引用新包新字段；两个指向 AnvilCraft 类的 mixin target（`AnvilMenuResult`、`WheelLifecycleEventListener`）在 2156 jar 中均存在。**版本号升至 `1.1.1`**（`gradle.properties` mod_version，区分已发布的 1.1.0），构建产物 `build/libs/anvilcraft_dearplus-neoforge-1.21.1-1.1.1.jar`。

### 6.10 会话 8 续：JEI 崩溃 + 飘升机 tooltip 修复（2026-08-16）

用户实测 1.1.1 发现两个问题并已修复：

**1. JEI 显示冲压配方崩溃**（`ClassCastException: AutumniumStampingRecipe cannot be cast to StampingRecipe`）
- 根因：AnvilCraft `StampingCategory.draw` 内有 `checkcast StampingRecipe`（`setRecipe` 用基类 `AbstractProcessRecipe` 可通过，但 `draw` 强转具体类）；附属配方注册在 `STAMPING_TYPE`，被 JEI 当 `StampingRecipe` 强转 → CCE
- 修复：`AutumniumStampingRecipe` 由 `extends AbstractProcessRecipe<AutumniumStampingRecipe>` 改为 **`extends StampingRecipe`**
  - 构造改为 `super(itemIngredients, List.of())`：**不把 results 传入 Property**——`AbstractProcessRecipe$Property.getOutcomes()` 会对每个 resultItem 自动生成 `SpawnItem`，传入会双刀
  - 产物 offset 改回 `-0.25`（StampingRecipe 默认 -0.375，本项目 `context.getPos()` 基于 pos 非 pos.below()）
  - `getSerializer()` 泛型由 `RecipeSerializer<AutumniumStampingRecipe>` 强转 `RecipeSerializer<StampingRecipe>`（unchecked）
  - `getType()` 继承 StampingRecipe（已返回 STAMPING_TYPE）
  - **执行回归修复**（用户实测 2026-08-16）：继承 StampingRecipe 后冲压执行物品消失无产物——`StampingRecipe` 构造在 `super()` 内用 `property.getOutcomes()` 生成 `InWorldRecipe.outcomes` 字段，且 `this.property` 字段在 super 后才赋值、`getOutcomes()` 是 extraOutcomes 的**拷贝**；构造体里 `addOutcome` 太晚 → `outcomes` 为空 → 无产物。修复：覆写 `assemble()`，`super.assemble`（predicates 消耗输入）后执行 `getProperty().getExtraOutcomes()`（词条合并 outcome）。执行入口 `InWorldRecipeManager.trigger` 调 `recipe.assemble`

**2. 飘升机 tooltip 换行显示异常字符**
- 现象：desc `"装备时允许创造飞行\n随时间消耗耐久，机制同鞘翅"` 的 `\n` 被渲染成异常字符（语言文件转义正确、MC `StringSplitter.LineBreakFinder` 本应处理 0x0A，但实测显示异常）
- 修复：`AddonTooltipEventListener` 按 `\n` split 成**多行独立 Component**（`Component.literal(line)`），不依赖渲染层对 `\n` 的处理

**3. AddonOutcomeTypes 注册实例统一**（H7 残留）
- `AFFIX_MERGE` 注册 supplier 由 `Type::new` 改 **`() -> AutumniumAffixMergeOutcome.Type.INSTANCE`**，使 `getType()` 返回的实例与注册表一致，`IRecipeOutcome.Type.getId()` 不再返回 null

**3. 高温熔炼配方 JEI 空流体槽位**（用户报告）
- 现象：五环刀→七环刀配方 `ringed_autumnium_broadsword_5_to_7.json` 在 JEI 显示一个空的流体原料槽位（配方无流体）
- 根因：JSON 的 `hasCauldron` 配置了 `{"fluid": "minecraft:empty"}`，AnvilCraft `AbstractLiquidCategory.setRecipe` 按 `hasCauldron.hasFluid()`（= `fluid.fluids().isPresent()`）决定显示输入流体槽位——empty 流体使 `fluids()` present → 显示空槽位
- 修复：删除 JSON 的 `hasCauldron` 字段 → Serializer `HasCauldronSimple.CODEC.fieldOf("hasCauldron").orElse(HasCauldronSimple.empty().build())` 生效 → `EMPTY_PREDICATE`（=`FluidStackPredicate.builder().amount(0)`，`fluids()` empty）→ `hasFluid()`=false 不显示槽位；`requiresEmptyCauldron()`=true 仍匹配空炼药锅（功能不变）

**4. 飘升机无法创造飞行**（用户报告，2026-08-16，**已回退，非本 mod 问题**）
- 现象：生存模式穿胸甲槽，双击空格无法获得飞行能力
- 尝试修复：曾改为服务端 `PlayerTickEvent.Post` + 物品类型判断管理 `CREATIVE_FLIGHT` modifier（与背包 `refreshFlight` 一致），但**用户实测发现并非本 mod 代码问题，而是与其他附属的兼容性问题** → **已回退修改**，恢复 `inventoryTick` + 引用比较原实现（`AutumniumIonocraftItem`、`AddonAffixEvents` 均还原）
- 调查记录（供参考）：AnvilCraft 背包飞行只靠 `CREATIVE_FLIGHT` modifier（不设置 `Abilities.mayfly`，`IPlayerExtension.mayfly()` 读属性值）；`Inventory.tick()` 会遍历 armor 槽调用 `inventoryTick`；飘升机原实现理论上正确

**避坑新增**：
- AnvilCraft 冲压 JEI 分类 `StampingCategory.draw` 会强转 `StampingRecipe`（2026-08-16 确认，snapshot.2156 起）；附属注册到 `STAMPING_TYPE` 的配方类必须继承 `StampingRecipe`
- `AbstractProcessRecipe$Property.getOutcomes()` 会对 resultItems 自动生成 `SpawnItem`，自定义 outcome 时**不要**把 results 放入 Property，避免白板产物
- 无流体配方**不要**用 `hasCauldron: {"fluid": "minecraft:empty"}`（`hasFluid()` 误判 true，JEI 显示空流体槽位）；应缺省 hasCauldron 字段让 `empty()` 生效（`EMPTY_PREDICATE` 匹配空炼药锅且 `hasFluid()`=false）
- 飘升机/背包飞行：`CREATIVE_FLIGHT` modifier 用 `IPlayerExtension.mayfly()` 驱动（检查 `Abilities.mayfly || 属性值 > 0`）。飘升机飞行若失效，**先排查与其他附属的兼容性**（2026-08-16 实测为环境问题），不要轻易改动 `AutumniumIonocraftItem` 原实现

## 七、最终功能清单

### 物品
| 物品 | 注册 id | 要点 |
|---|---|---|
| 秋枫合金 / 合金块 | `autumnium_alloy` / `autumnium_alloy_block` | 基础材料：合金可由 铜锭+陶瓦+`is_autumnium_component` 合成；9 合金 ↔ 1 合金块 |
| 重锻面板 | `reforging_panel` | 锻星砧接口：GUI 配筛选条件、红石自动重锻、天体匹配时发红石、active 模型 |
| 秋枫共振器 | `autumnium_resonator` | 偏安；254耐久/铁级挖掘；仅共振器形态；可共振挖掘；不可铁砧修复 |
| 秋枫飘升机 | `autumnium_ionocraft` | 偏安；432耐久/护甲+4；创造飞行、飞行耗耐久、损毁纹理；穿戴用背包模型；粒子复用本体离子背包排气粒子 |
| 秋枫三/五/七/九环刀 | `ringed_autumnium_broadsword_3/5/7/9` | 见下 |

### 词条（组件驱动）
| 词条 | 注册名 | 效果 |
|---|---|---|
| 偏安 | `tranquil` | 不可附魔/祛魔/铁砧修复；附魔失效（吸收到 `TRANQUIL_ENCHANTMENTS`） |
| 逐鹿 | `vying`（带等级，上限 10） | 附魔免经验免等级、费用 0、不增加 REPAIR_COST、稀有掉落概率 P → P+(1-P)×L/10（`random_chance_with_enchanted_bonus` 条件，10级=必定） |
| 破竹 | `chopper` | = 锋利 |
| 枭首 | `decapitator` | = 斩首 |
| 豪夺 | `dispossessor` | = 抢夺 |

### 大环刀特性
- 升级链：合成→冲压→高温熔炼→二合一锻造；词条由金属粒决定（铁/铜/金→破竹/枭首/豪夺）
- 词条只与被消耗金属粒相关、一次只加工一把
- 真实横扫（命中/挥空均触发大范围横扫，命中不受原版 flag2 疾跑/站地限制；伤害 = 直击 × √(横扫之刃等级+1) 向下取整，无附魔 = 直击）
- 对亡灵生物伤害翻倍（`EntityTypeTags.UNDEAD`）
- 九环刀：逐鹿；铁砧合并取最小+魔咒合并+费用 0
- **九环刀词条叠加**（2026-08-14）：二合一锻造，材料槽=超限多相合金（`anvilcraft:multiphase_transcendium`），输入槽=两把九环刀 → 新九环刀；破竹/枭首/豪夺取两把之和（上限 255）；结果必带「永恒」词条（`ModComponents.ETERNAL`）；附魔取两把每级最大值（不考虑互斥）；**逐鹿等级**：两把相同则 +1、不同则取最大（上限 10，`VyingMergeData`）。配方 `anvilcraft:two_to_one_smithing/ringed_autumnium_broadsword_9_merge`
- **逐鹿带等级**（2026-08-14 重构）：`AddonComponents.VYING` 由 `Unit` 改为 `Integer`；九环刀初始逐鹿等级 1；`LootItemRandomChanceWithEnchantedBonusConditionVyingMixin` 提升稀有掉落概率：**P → P+(1-P)×L/10**（L=逐鹿等级，10 级必定掉落）；已放弃"强运"（PROVIDENCE）方案（PROVIDENCE 单例无法表示等级，且时运/掉落数量是两个战利品函数类无法合并 mixin）
- 不可铁砧修复；工作台+合金块修复

### 重锻面板特性
- 接口方块：仅可紧贴锻星砧底边水平放置、方向背对锻星砧；锻星砧破坏时面板不连锁破坏
- 筛选条件（GUI）：天体磁场 / 天体转速 / 行星特殊资源（任意/生物/文明/无），每项可选择参与/不参与
- 自动重锻：收到红石信号即开始；天体匹配条件或无法锻造时不重锻
- 红石输出：面板激活且天体匹配时，向上/下/FACING 方向发出强度 3 弱充能；仅水平侧面输入可激活
- 状态区分：`POWERED`（模型激活/重锻中）与 `EMITTING`（天体匹配）分离；重锻中不发射信号

---

## 八、关键技术要点（避坑）

1. **3D 物品模型不能用 `parent: item/handheld`**（根为 generated → 平面化，忽略 elements）。应无 parent、自带 `display`。
2. **Java 模型 UV 0-16 归一化到整个纹理**（`FaceBakery` 除 16），32x32 纹理配 0-16 UV，不要乘 2。
3. **MixinExtras 0.5.3 的 `@ModifyExpressionValue` 不支持 FIELD**，字段读取用 `@Redirect` / `@WrapOperation`。
4. `mixins.json` 有 `defaultRequire: 1`，注入不匹配会加载失败。
5. 铁砧工艺变体（余烬/浮霜/皇家/超脱）`extends AnvilMenu` 但覆写 createResult，用 `AnvilMenuResult`（旧 REPAIR_COST 算法）。
6. `ItemStack.isEnchantable()` 依赖空 `ENCHANTMENTS` 组件（默认有）。
7. 铁砧费用显示统一入口 `AnvilMenu.getCost()`。
8. **GUI display 里 +y = 上**（实测确认），-y = 下。
9. 负方块（from > to）是故意的，影响面渲染，**不要规范化**。
10. 逐鹿/偏安组件用 `Codec.EMPTY` 序列化（`MapCodec<Unit>`），保存加载正常。
11. **GUI 渲染递归坑**：`renderBg()` 内不要调 `renderBackground()`——AnvilCraft 的 `AbstractContainerScreenMixin` 会改写它导致无限递归 StackOverflow。
12. **CFA 红石感知读强充能**：锻星砧用 `getBestNeighborSignal()`，弱充能不够；改发射状态后需主动 `markRedstoneSignalDirty()` 刷新。
13. **`getSignal` 的 direction 参数**方向约定易反；`FACING` 远离锻星砧，目标方向 = `FACING.getOpposite()`。
14. **模型激活与红石发射分离**：`POWERED`（模型）与 `EMITTING`（天体匹配）用不同方块状态属性，避免重锻中误发信号。
15. **Mixin 先找公开 API**：AnvilCraft 1.6.0 已有公开 API 时可替代自写 Mixin（曾移除 `CelestialForgingAnvilBlockEntityMixin`）。
16. **`.save(prov, name)` 命名空间**：不指定默认 `minecraft`，需显式传本模组 `ResourceLocation`。
17. **CFA 接口方块放置限制**：重写 `getStateForPlacement` + `neighborChanged`（1.21.1 无 `canSurvive` 签名）实现紧贴与方向固定。
18. **多物品共享 display**：把相同的 `display`+`gui_light` 抽到公共父模型，子模型 `parent` 继承（MC 会合并 display 视角），各子模型只留 textures+elements。
20. **`TieredItem` 不覆写 `isCorrectToolForDrops`**：默认按 `Tool` 组件的 `minesAndDrops` 规则判断，**不校验 tier 等级**。`ResonatorItem`（AnvilCraft）同样未覆写 → 会误挖黑曜石/远古残骸并掉落。自建工具需像 `PickaxeItem` 一样覆写该方法，结合 `getTier().getIncorrectBlocksForDrops()` 判断等级（秋枫共振器 2026-08-14 已修）。
21. **`Enchantments.SWEEPING_EDGE` 是 `ResourceKey` 不是 `Holder`**：取附魔等级需 `level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(...)` 再 `stack.getEnchantmentLevel(holder)`。
22. **`StreamCodec.unit(...)` 编码校验引用相等**：配方运行时创建的新实例与 unit 实例不同会抛 `Can't encode ... expected ...`。无字段类应改用 `StreamCodec.of((buf, v) -> {}, buf -> new X())`（编码写空、解码返回新实例）；`MapCodec.unit` 的 encode 不校验，可安全用于无字段类（九环刀合并 modifier 2026-08-14 已修）。
23. **AddonConfig 配置格式**（`dev.anvilcraft.lib.v2.config`，`AddonConfig.java`，运行时经 `AnvilCraftDearPlus.CONFIG` 读取）：`@Config(name=MOD_ID)` 类 + 字段 `@Comment("...")`；boolean 直接 `public boolean xxx = false;`；受限整数加 `@BoundedDiscrete(max=, min=)`；字符串 `public String xxx = "";`（模板自带的 `logDirtBlock`/`magicNumber`/`magicNumberIntroduction` 示例已删除，仅保留 `affixNumberStyle`）。词条等级罗马/阿拉伯显示由 `util/AffixNumberFormat` 处理（混合/全罗马/全阿拉伯）。
19. **模型体积差异 = 格式**：Blockbench 紧凑单行 vs 展开多行（每个数组元素一行）可差近一倍；压成紧凑格式（数组内联、面单行）能缩小文件且 JSON 合法，注意**不能有尾逗号**。

---

## 九、当前文件状态

- 4 把刀模型：`src/main/resources/assets/anvilcraft_dearplus/models/item/ringed_autumnium_broadsword_{3,5,7,9}.json`（紧凑格式，`parent` 指向公共 display 父模型 `ringed_autumnium_broadsword_display.json`；2026-08-13 重构，消除重复 display，4 刀+父模型 50.6KB → 31.3KB）
- 刀贴图：`textures/item/ringed_autumnium_broadsword.png`（32x32）
- 飘升机：穿戴用 AnvilCraft 飘升机背包模型（`AutumniumIonocraftItem.getHumanoidArmorModel`）+ `textures/entity/equipment/autumnium_ionocraft.png`（64×32）；物品图标 `textures/item/autumnium_ionocraft.png`（16×16）；`getArmorTexture` 返回设备贴图
- 新增 mixin：`AnvilMenuResultMixin`、`EnchantmentScreenMixin`
- 修改 mixin：`AnvilMenuMixin`、`EnchantmentMenuMixin`、`PlayerMixin`、`GrindstoneMenuMixin` 等
- 核心：`AddonComponents`、`AffixHelper`、`BladeAffixes`、`AddonTagHandler`、`AddonRecipeHandler`
- 重锻面板：`block/ReforgingPanelBlock.java`、`block/entity/ReforgingPanelBlockEntity.java`、`inventory/ReforgingFilter(Data).java`、`inventory/ReforgingPanelMenu.java`、`client/gui/screen/ReforgingPanelScreen.java`、`init/ModBlockEntities.java`、`init/ModMenuTypes.java`
- 重锻面板资源：`blockstates/reforging_panel.json`、`models/`、`textures/`、`assets/anvilcraft/ageratum/zh_cn|en_us/173_dearplus/`
- ageratum 手册（2026-08-14 整理完整，`assets/anvilcraft/ageratum/zh_cn|en_us/173_dearplus/`）：`index.md`（总览）+ `000_alloy`（秋枫合金/合金块）+ `001_resonator` + `002_ionocraft` + `003_broadsword`（四档属性/锻造升级/真实横扫/亡灵特攻）+ `004_affixes`（词条）+ `005_reforging_panel`（重锻面板，会话 6 由 001 移至末章）
- 代码审查报告（2026-08-15）：`CODE_REVIEW_REPORT.md`（根目录）——16 高 + 17 中 + 多项低严重度隐患，含修复进度核对表（⬜/🔧/✅），修复时可对照勾选

---

## 十、当前状态与待办

- ✅ 核心功能全部实现并验证（编译/服务器/客户端启动通过）
- ✅ 铁砧工艺依赖已更新至 `1.6.0+snapshot.2156`（AnvilLib 同步 `2.0.0+snapshot.506`，与内嵌版本匹配；workflow extra-mods/发布依赖/mods.toml versionRange 均已同步；2026-08-13 更新至 2144，2026-08-16 更新至 2156）
- ✅ 4 把刀模型已应用，GUI 显示当前值：`rotation [0,20,0], translation [-0.7,-0.55,0], scale 0.65`
- ✅ 已补充会话 `3eb0b138`（基础物品 + 重锻面板）全部需求至本交接文档
- ✅ 4 把刀物品栏渲染已确认（2026-08-13）
- ✅ 重锻面板重锻功能已复测正常（2026-08-13）
- ✅ 飘升机模型已确认为正式（2026-08-13）：穿戴 = AnvilCraft 飘升机背包模型 + 自定义 64×32 贴图；物品 = 16×16 图标；已删除无用 `textures/models/armor/autumnium_ionocraft_layer_1.png`
- ✅ 共振器/大环刀物品栏裁剪：已注册 `ItemSlotClipping`（2026-08-13，注册在 FMLClientSetupEvent，避免 unbound 异常）；大环刀 GUI 最终值 `rotation [0,20,0], translation [-2.05,-1.9,0], scale 0.7`
- ✅ 秋枫共振器正式贴图已应用（2026-08-14）：`textures/item/autumnium_gear.png`（32×32），模型 `autumnium_resonator.json` 覆盖 `textures."1"` 引用之，模型结构保持共振器统一模型（parent `anvilcraft:item/ember_metal_resonator`）
- ✅ 重锻面板可被铁砧锤拆下（2026-08-14）：`ReforgingPanelBlock` 实现 `IHammerRemovable`；拆下走 silk-touch 掉落自身，不保留 BE 筛选数据（与 AnvilCraft 接口方块一致）
- ✅ 大环刀「横扫之刃」联动（2026-08-14）：真实横扫伤害 = `直击×√(横扫之刃等级+1)` 向下取整，命中不受 flag2 限制；横扫之刃附魔可增强命中/挥空横扫
- ✅ 大环刀对亡灵生物伤害翻倍（2026-08-14）：`AddonAffixEvents.onDamageUndead`，`LivingIncomingDamageEvent` 中目标为 `EntityTypeTags.UNDEAD` 且攻击者主手为大环刀（`instanceof RingedAutumniumBroadswordItem`，会话 6 由 ModTags 统一）且**近战**（`PLAYER_ATTACK`）时 `setAmount(amount*2)`
- ✅ 逐鹿设定重构完成（2026-08-14）：`VYING` 带等级（上限 10）、稀有掉落概率 `P+(1-P)×L/10`（P=0 时 P×L）、罗马数字/MAX 显示、config 可选
- ✅ 九环刀词条叠加机制完成（2026-08-14）：超限合金二合一 + 铁砧合并（永恒/附魔取最大/逐鹿升级/REPAIR_COST 清零）
- ✅ ageratum 手册整理完整 + 代码审查报告导出（2026-08-15）
- ✅ 代码审查修复完成（2026-08-15，会话 6）：8 高 + 9 中 + 5 低共 22 项，详见 6.8 与 `CODE_REVIEW_REPORT.md` 核对表
- ✅ 大环刀实际攻击 6/7/8/12（2026-08-15）：modifier 公式修正，注释/手册/交接文档同步
- ✅ 共振器耐久 254、攻击 6（2026-08-15）：耐久避开词条上限 255 撞车；攻击按 `ResonatorItem.createAttributes` 公式修正
- ✅ 飘升机粒子复用本体 `ModParticles.IONOCRAFT_BACKPACK_EXHAUST`（2026-08-15），删除自建粒子类型/类/贴图
- ✅ 飘升机多人粒子飞行同步（2026-08-15）：S2C `AutumniumIonocraftFlyingPacket` + 客户端 `SYNCED_FLYING_PLAYERS`（照搬本体背包机制）
- ⏳ **待办**：代码审查隐患修复，详见根目录 `CODE_REVIEW_REPORT.md` 进度核对表。**2026-08-15（会话 6）已修 8 高 + 9 中 + 5 低**；其余 6 高 + 7 中决策不改、2 高（H6/H12）+ 1 中（M10）暂缓/搁置
