# 代码审查报告

- **项目**：AnvilCraft-DearPlus-CelestialReforge（Minecraft 1.21.1 + NeoForge 21.1.241，分支 `releases/1.21.1/1.6`）
- **日期**：2026-08-15
- **范围**：`src/main/java` 全部 70+ 文件（含构建/资源/语言一致性），由 4 组并行审查 Agent + 全局补充完成
- **用途**：记录所有潜在隐患点，供后续修复与进度核对

**状态约定**：⬜ 待修复 / 🔧 修复中 / ✅ 已修复

---

## 一、修复进度总览

| 严重度 | 数量 | 已修复 |
|---|---|---|
| 高 | 16 | 8 |
| 中 | 17 | 9 |
| 低 | 多项 | 0 |

---

## 二、高严重度（16 项）

### 高耗性能

- **H1** ⬜〔已决策：不改，与 AnvilCraft 本体「无情」`Merciless.tick` 同款每 tick 全背包扫描设计〕`event/AddonAffixEvents.java` `onPlayerTick`（L52-59）
  - 类型：高耗性能
  - 问题：每玩家每 tick 全背包扫描（41 槽位 + `stack.has()` 组件读取），偏安物品常驻时永不停止。50 人服 ≈ 4 万次 `has()`/秒 + 41 引用列表分配/人/秒。
  - 建议：改为低频（如 `gameTime % 10 == 0`）或事件触发（`InventoryChangedEvent` / 附魔后置处理），避免常驻全量扫描。

- **H2** ⬜〔已决策：不改，单次评估为廉价 getter，仅 1 次 getBlockEntity；影响可忽略〕`block/entity/ReforgingPanelBlockEntity.java` `tick`（L94-120）
  - 类型：高耗性能
  - 问题：红石持续为高、冷却为 0 且砧上无生物时，**每 tick** 执行 `findCfaController()`、`getEffectiveBodyDataForRendering()`、`getPlanetaryResourceSet()`、`matchesFilter()`、`canReforge()` 多次方块/实体查找。
  - 建议：匹配评估节流（10-20 tick 或仅 `markRedstoneDirty`/搜索完成时评估），无生物时进入长冷却。

- **H3** ⬜〔已决策：不改，真实横扫「打一片」为设计卖点，接受密集场景卡顿〕`util/TrueSweepHelper.java` `sweepAround`（L61-77）
  - 类型：高耗性能
  - 问题：每次横扫对 5×2×5 AABB（`inflate(2.5,1.0,2.5)`）做 `getEntitiesOfClass` 全量实体扫描 + 对每个目标 `knockback`+`hurt`（完整伤害事件链）+`doPostAttackEffects`。刷怪塔内数百实体时一次挥刀 = 数百次伤害结算。
  - 建议：限制每次横扫实体数量上限（最近 N 个）、按密度降级、AABB 缩小或预筛选。

### 正确性 / 高侵入

- **H4** ✅ `mixin/LootItemRandomChanceWithEnchantedBonusConditionVyingMixin.java` `vyingBoostRareDrop`
  - 类型：正确性
  - 问题：**双重掷骰数学错误**。`if (original) return true;` 后再以 `boosted = min(1, p + (1-p)L/10)` 掷一次，实际最终概率 = `p + (1-p)×min(1, p+(1-p)L/10)`，与注释公式 `P + (1-P)L/10` 不符（例 p=0.1、L=5：注释目标 0.55，实际 0.595）。
  - 建议：不做 `original` 早退，直接 `return context.getRandom().nextFloat() < min(1, p + (1-p)L/10)`。

- **H5** ✅ `block/entity/ReforgingPanelBlockEntity.java` + `block/ReforgingPanelBlock.java`
  - 类型：高侵入/正确性
  - 问题：面板在"天体匹配"分支置 CFA `locked=true`（L108），但 deactivate 分支（L87-91）**从不 `setLocked(false)`**，且面板移除时 `ReforgingPanelBlock` 无 `onRemove` 清理——CFA `locked` 永久残留并持久化到 NBT，与玩家 CFA UI 共享同一状态却互不协调。
  - 建议：deactivate 分支显式解锁；`ReforgingPanelBlock.onRemove` 里找到 controller 并 `setLocked(false)`（仅当锁定系本面板所致）。

- **H6** ⬜〔已决策：暂缓，当前正确；依赖「accept 时世界未 sync」时序，配合 H16 版本策略一并处理〕`recipe/AutumniumAffixMergeOutcome.java` `accept`（L47-110）
  - 类型：低兼容/正确性
  - 问题：用"世界实体原始量 − ItemCache 剩余量"反推金属粒消耗量，强耦合 anvillib/AnvilCraft 执行时序（matches snapshot shrink → assemble → accept 后 endCache）。时序一改即 `consumed` 归 0 → **升级产物词条全部静默丢失**。
  - 建议：在 matches/snapshot 阶段记录每种金属粒原始总量，或让 outcome 直接读配方谓词暴露的消耗量，避免世界扫描反推。

- **H7** ✅ `recipe/AutumniumAffixMergeOutcome.java` `Type`（L118-129）
  - 类型：低兼容
  - 问题：`AutumniumAffixMergeOutcome.Type` **从未注册到 `LibRegistries.OUTCOME_TYPE_REGISTRY`**，`getId()` 返回 null。任何枚举/序列化 outcomes 的代码会 NPE（当前 heating/stamping 用自建 serializer 不序列化 outcomes 才侥幸无事）。
  - 建议：注册类型，或改回框架 `SpawnItem` + `ICustomDataComponent` 方式删除自定义 outcome。

- **H8** ✅ `recipe/AutumniumHeatingRecipe.java`（L66-91）+ `AutumniumStampingRecipe.java`
  - 类型：结构/兼容
  - 问题：构造器收到的 `results` 参数**从未被 `setResultItems(results)` 消费**，但配方 JSON 里写了 `"results"` 字段——被 codec 解析后静默丢弃。`getResultItems()` 返回空 → `getIcon()` 落到 `Items.ANVIL`，JEI/EMI/配方书无法显示真实产物；改 JSON 的 `results` 无效。
  - 建议：删除 `results` 字段（或删 `target`，用 `results` 作为唯一输出源）。

- **H9** ⬜〔已决策：不改，方案A getResultItem=EMPTY 与 ItemStack.STREAM_CODEC 序列化冲突（配方网络同步崩溃），已回退〕`recipe/RingedAutumniumBroadswordRepairRecipe.java`
  - 类型：结构/兼容
  - 问题：JSON `result` 硬编码 `ringed_autumnium_broadsword_3`，但 `assemble()` 实际返回**输入刀复制 + 满耐久**（任意 3/5/7/9 环刀）。JEI/配方书显示"3 环刀 + 合金块 → 3 环刀"，实际是"任意环刀 → 同款满耐久"，展示与实际完全不符。
  - 建议：改用 `SpecialRecipe`/自定义 serializer，或让 `getResultItem` 返回空/首个匹配输入。

- **H10** ✅ `item/property/component/BladeAffixes.java` + `AutumniumAffixMergeOutcome` + 升级路径（仅 `add()` clamp 至 255，codec 校验未加）
  - 类型：结构
  - 问题：词条上限体系不一致。二合一合并 `DoubleBladeAffixesData` 有 `MAX_LEVEL=255` cap，但升级路径（3→5→7→9）`BladeAffixes.add` + `AutumniumAffixMergeOutcome` **无上限**，可无限堆金属粒绕开 255；`BladeAffixes` codec（`Codec.INT`/`VAR_INT`）也无校验。
  - 建议：上限收敛到 `BladeAffixes.add`/codec 统一（如 `Codec.intRange(0, MAX)`），升级路径同样 clamp。

- **H11** ✅ `init/AddonItems.java` `ringBlade`（L119-124）
  - 类型：数据/平衡
  - 问题：`SwordItem.createAttributes(tier, modifier, speed)` 实际攻击 = 1 + modifier + tierBonus，而 modifier = `attackDamage - (int)tierBonus`，故最终 = **attackDamage + 1**。注释声称 3/5/7/9 刀攻击 5/5/6/12，实际 6/6/7/13，全部偏高 1 点。
  - 建议：modifier 应为 `attackDamage - 1 - (int)tierBonus`，或把注释改为实际值。

### 低兼容性 / 脆弱

- **H12** ⬜ `mixin/PlayerMixin.java` `trueSweepOnAttack`（L59-78）
  - 类型：低兼容
  - 问题：`@Local(ordinal=3) float f3`、`@Local(ordinal=0) ItemStack weapon` 按局部变量表槽位捕获，经字节码验证当前正确，但依赖编译器局部变量表顺序+作用域，版本升级/反混淆差异会**静默取错变量**（伤害为 0 或取错 float）。
  - 建议：改为 `@Local` 直接传参或自算伤害，减少 ordinal 依赖。

- **H13** ✅ `util/TrueSweepHelper.java` `ATTACK_SWING_TICKS`（L33-44）
  - 类型：性能/内存泄漏
  - 问题：`HashMap<UUID, Long>` 只增不删，无任何清理路径；且无锁，脱离主线程调用会并发损坏。
  - 建议：`PlayerLoggedOutEvent` 移除 / 改实体附属数据 / 定期清理 + 容量上限。

- **H14** ✅ `mixin/PlayerMixin.java` + `mixin/MinecraftAttackMixin.java`
  - 类型：健壮性
  - 问题：ThreadLocal（`ATTACKING`/`CLIENT_ATTACKING`）在 HEAD 置 true、RETURN 置 false，**无 try/finally**。`startAttack`/`attack` 中途异常即永久卡 true，横扫特性全局失效至重启。
  - 建议：用 `WrapOperation`/`finally` 保证复位。

- **H15** ⬜〔已决策：不改，保持 defaultRequire=1 崩溃式报错（require=0 会导致静默失效更糟）+ 版本范围无界〕`anvilcraft_dearpluscelestialreforge.mixins.json`
  - 类型：低兼容
  - 问题：`required: true` + `defaultRequire: 1` + `overwrites.requireAnnotations`。12 个 mixin 任一注入点不匹配（含对 AnvilCraft 私有方法的注入）即**加载崩溃**，无降级容错。
  - 建议：低风险注入用 `require=0` 或拆分 mixin config；文档/CI 固定 AnvilCraft 版本。

- **H16** ⬜〔已决策：不改，版本依赖保持 [1.6.0,) 无界以兼容 AnvilCraft 正式版，接受 CFA 内部 API 兼容风险〕`gradle/libs.versions.toml` + `src/main/templates/META-INF/neoforge.mods.toml`
  - 类型：低兼容
  - 问题：编译锁定 `anvilcraft = 1.6.0+snapshot.2144`，但 mods.toml 声明 `versionRange = "[1.6.0,)"` 无界。重锻面板大量用 CFA 内部 API（`getEffectiveBodyDataForRendering`、`getPlanetaryResourceSet`、`startSearch` 等），正式版内部改动即崩溃。
  - 建议：收紧版本范围到当前快照，或对 CFA 方法加运行时保护/兼容层。

---

## 三、中严重度（17 项）

- **M1** ✅ `AddonAffixEvents.onIncomingDamage/onDamageUndead`：药水/间接伤害（direct entity=玩家）也触发破竹加成和亡灵×2，未校验伤害来源是武器近战。
- **M2** ✅ `AutumniumIonocraftItem.tickFlight`：两件飘升机互相删飞行 modifier（穿着件加、背包件删），飞行失效/闪烁；`==` 引用相等脆弱。
- **M3** ✅ `TrueSweepHelper`：横扫两路径伤害口径不一致（attack 用 f3 完整伤害，swing 用裸属性）；1 tick 容差对网络延迟敏感易漏触发。（①伤害口径确认不改：蓄力≥0.9 才横扫，裸属性即满蓄力伤害；②容差放宽至 3 tick）
- **M4** ⬜〔已决策：不改，真实横扫打所有为预期行为，与 H3 一致〕`TrueSweepHelper`：横扫 5×2×5 范围误伤友军/中立/玩家；原版横扫面积小且仅命中触发。
- **M5** ⬜〔已决策：不改，影响微小且立即拾取对玩家无害〕`AddonAffixEvents.growDrop/applyDecapitator`：新增掉落物 `pickupDelay=0`（原版 `setPickUpDelay(10)`），可被立即拾取且无初速度。
- **M6** ✅ `AddonAffixEvents.onLivingDrops`：只认 `getDirectEntity() instanceof Player`，弓箭远射不触发枭首/豪夺；PvP 掉无皮肤的 Steve 头。（只修 PvP 头 profile；弓箭场景大环刀为近战不成立）
- **M7** ✅ `RingedAutumniumBroadswordItem` + mixin + events：武器判定口径不一致（横扫用 `instanceof`，亡灵×2 用 `ModTags`），外部加入标签的刀只获一半效果。（统一为 instanceof，大环刀特性严格限本 mod 物品）
- **M8** ⬜〔已决策：不改，三个子问题当前均不触发或开销可忽略〕`AddonTooltipEventListener`：desc 无翻译兜底（缺失显示原始键）；`add(index)` 潜在越界；`moveEternalToTop` 全局扫所有物品 tooltip。
- **M9** ⬜〔已决策：不改，mixin 已拦截偏安附魔，仅外部感知〕`AddonTagHandler`：大环刀直接写入原版 `#minecraft:swords`，影响所有基于该标签的机制及其他 mod。
- **M10** ⬜〔已决策：搁置不改，转速 5 天体概率极低（0.1%）〕`ReforgingFilter`：旋转速度筛选上限 4，但天体自转可达 5——速度 5 的天体无法被选中。
- **M11** ✅ `LivingEntitySweepMixin.sendAttackSwing`：所有玩家每次左键攻击无条件发网络包（不检查是否大环刀），服务端写 HashMap。
- **M12** ✅ `AutumniumIonocraftParticleHandler`：远程玩家 `getAbilities().flying` 不可靠（vanilla 只同步本地玩家），其他人飘升机从不喷粒子。（照搬 AnvilCraft 背包机制：S2C 飞行同步包 + SYNCED_FLYING_PLAYERS）
- **M13** ⬜〔已决策：不改，①②③影响均小且①②无法有效避免/有风险〕`ReforgingPanelBlockEntity`：`matchesFilter` 每 tick 4 次 `unmodifiableList` 分配；`setEmitting` 双重邻居通知；`getEffectiveBodyDataForRendering` 依赖 CFA 渲染字段。
- **M14** ⬜〔已决策：不改，与 H16 版本策略一致〕`AddonBlocks`/`AddonRecipeHandler`：深度依赖 AnvilCraft 专属物品/内部 API（`TwoToOneSmithingRecipe`、CFA 方法），快照升级即断裂。
- **M15** ⬜〔已决策：不改，手写 serializer 是自定义 outcome 设计的必要部分，当前正确〕手写 serializer：heating/stamping/repair 绕过框架基类，用 `BuiltInRegistries`（非数据包感知），手写 StreamCodec 易错。
- **M16** ✅ `ReforgingPanelMenu.stillValid`：恒 true，面板破坏/玩家离开后 GUI 不关闭。
- **M17** ✅ 词条上限魔法数字：tooltip 用字面量 `255`/`10`，未引用 `MAX_LEVEL` 常量；`AddonTiers` 耐久 255 与词条上限撞车。（tooltip 改常量引用；共振器耐久 255→254 避免撞车）

---

## 四、低严重度（择要）

- **结构混乱**：`AnvilMenuMixin` 两处"费用归零"逻辑重复（ModifyArg + getCost）；`BladeAffixesData` 与 `DoubleBladeAffixesData` 命名/Javadoc 混淆；heating/stamping `matches()` 逐字重复；`RingedAutumniumBroadswordCraftingRecipe` 二次存储 pattern 字段 ✅已修；空 `register()` 方法误导；`AddonDatagen` 空监听死代码 ✅已修；`ReforgingPanelScreen` GUI 高度 195 vs 背景 78 下方空区。
- **低兼容性**：`AutumniumResonatorItem` 构造未设默认 TOOL 组件（✅已确认误报，基类 `ResonatorItem` 已设置）；`getBaseAttackDamage()` 返回硬编码 7 ✅已提常量；`WheelLifecycleEventListenerMixin` 硬编码 AnvilCraft 内部类名；`EnchantmentScreenMixin` `@Redirect` 字段读取最脆弱；`AutumniumIonocraftItem` 硬依赖 AnvilCraft 内部模型 API；`ReforgingFilterData` 反序列化不校验值范围 ✅已加 clamp。
- **依赖/配置**：`anvilcraft` 关闭传递 + 手动 anvillib（版本需人工对齐）；中文语言缺 5 条 config 翻译（`affix_number_style` 等）✅已补；`duplicatesStrategy = WARN` 掩盖资源覆盖；`@BoundedDiscrete` 魔法数字。
- **健壮性**：`WheelLifecycleEventListenerMixin` 对 `Minecraft.getInstance()` 未判空；`EnchantmentMaxMergeData` 合并后结果可带互斥附魔（设计决定但需明示）；`VyingMergeData` 对负数等级未防御；`TranquilToEnchantmentsData` 搬附魔不校验结果上的 TRANQUIL 组件。

---

## 五、已验证「当前正确但脆弱」的点（勿误修）

- 各 mixin 注入点（`getCost`、`calculateIncreasedRepairCost`、`getAbilities`、`experienceLevel` 字段读、`Entity.hurt` 的 f3 等）经字节码反编译验证**当前参数/顺序正确**。
- `AutumniumAffixMergeOutcome` 的"世界原始量 − cache 剩余量"在**当前 anvillib 时序下成立**（matches snapshot shrink → assemble → accept 后 endCache）。
- `@Local` ordinal 捕获（f3/weapon）当前命中正确，但属版本脆弱。
- `AutumniumResonatorItem` 继承 `ResonatorItem` 可正常共振挖掘；逐鹿/偏安组件用 `Codec.EMPTY` 序列化正常。

---

## 六、全局层面隐患（补充）

1. **依赖配置**：`dependencies.gradle` 中 `anvilcraft` 关闭传递依赖 + 手动 `anvillib`，版本需人工对齐（AnvilCraft 内嵌 anvillib 更新而项目未同步会运行时类冲突）。
2. **中文 config 翻译缺失**：`en_us.json` 有 5 条 `affix_number_style` 等 config 条目，`zh_cn.json` 无对应翻译。
3. **`duplicatesStrategy = WARN`**：资源重复只警告不报错，可能掩盖生成/手动资源覆盖问题。
4. **大量生成资源未提交**：`src/generated` 的配方/进度等全部未跟踪，发布时需注意。
