package dev.anvilcraft.addon.dearpluscelestialreforge.client.gui.screen;

import dev.anvilcraft.addon.dearpluscelestialreforge.init.ReforgingFilter;
import dev.anvilcraft.addon.dearpluscelestialreforge.inventory.ReforgingPanelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ReforgingPanelScreen extends AbstractContainerScreen<ReforgingPanelMenu> {
    private static final int GUI_WIDTH = 220;
    private static final int GUI_HEIGHT = 195;
    private static final int ROW_HEIGHT = 28;
    private static final int CHECKBOX_WIDTH = 60;
    private static final int VALUE_BTN_WIDTH = 100;

    private Button[] valueButtons;
    private CheckboxButton[] checkboxButtons;

    public ReforgingPanelScreen(ReforgingPanelMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        int left = leftPos;
        int top = topPos;
        ReforgingFilter[] filters = ReforgingFilter.values();
        valueButtons = new Button[filters.length];
        checkboxButtons = new CheckboxButton[filters.length];

        for (int i = 0; i < filters.length; i++) {
            int y = top + 18 + i * ROW_HEIGHT;
            int ordinal = i;
            ReforgingFilter filter = filters[i];
            var filterData = menu.getFilterData();

            // 勾选框（参与/不参与筛选）
            checkboxButtons[i] = new CheckboxButton(
                left + 6, y, 80, 20,
                isEnabled(ordinal),
                btn -> {
                    // 乐观更新本地数据
                    int idx = ordinal * 2;
                    filterData.set(idx, filterData.get(idx) == 0 ? 1 : 0);
                    // 发送到服务端
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ordinal);
                }
            );
            addRenderableWidget(checkboxButtons[i]);

            // 值循环按钮
            valueButtons[i] = Button.builder(
                    Component.literal(getValueText(ordinal)),
                    btn -> {
                        // 乐观更新本地数据（正向）
                        int idx = ordinal * 2 + 1;
                        int cur = filterData.get(idx);
                        int max = filter.getMaxValue();
                        filterData.set(idx, (cur + 1) % (max + 1));
                        // 发送到服务端
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 10 + ordinal);
                    }
                )
                .bounds(left + 88, y, VALUE_BTN_WIDTH, 20)
                .tooltip(Tooltip.create(Component.translatable(
                    "gui.reforging_panel.cycle_" + filter.getSerializedName())))
                .build();
            addRenderableWidget(valueButtons[i]);
        }

        // 保存按钮
        addRenderableWidget(Button.builder(
                Component.translatable("gui.reforging_panel.save"),
                btn -> onClose()
            )
            .bounds(left + 70, top + GUI_HEIGHT - 28, 60, 20)
            .build()
        );
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        // 每 tick 同步按钮文本（处理 DataSlot 同步后的更新 + 首次打开时的初始同步）
        for (int i = 0; i < valueButtons.length; i++) {
            if (valueButtons[i] != null) {
                valueButtons[i].setMessage(Component.literal(getValueText(i)));
            }
            if (checkboxButtons[i] != null) {
                checkboxButtons[i].updateState(isEnabled(i));
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 右键点击值按钮 → 反向循环
        if (button == 1) {
            for (int i = 0; i < valueButtons.length; i++) {
                if (valueButtons[i] != null && valueButtons[i].isMouseOver(mouseX, mouseY)) {
                    // 乐观更新本地数据（反向）
                    int idx = i * 2 + 1;
                    int cur = menu.getFilterData().get(idx);
                    int max = ReforgingFilter.values()[i].getMaxValue();
                    menu.getFilterData().set(idx, (cur - 1 + max + 1) % (max + 1));
                    // 发送到服务端
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 20 + i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (int i = 0; i < valueButtons.length; i++) {
            if (valueButtons[i] != null && valueButtons[i].isMouseOver(mouseX, mouseY)) {
                int idx = i * 2 + 1;
                int cur = menu.getFilterData().get(idx);
                int max = ReforgingFilter.values()[i].getMaxValue();
                if (scrollY > 0) {
                    // 上滚 → 正向
                    menu.getFilterData().set(idx, (cur + 1) % (max + 1));
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 10 + i);
                } else {
                    // 下滚 → 反向
                    menu.getFilterData().set(idx, (cur - 1 + max + 1) % (max + 1));
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 20 + i);
                }
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // 背景由 AbstractContainerScreen.render() 自动渲染（含 AnvilCraft Mixin）
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ReforgingFilter[] filters = ReforgingFilter.values();
        for (int i = 0; i < filters.length; i++) {
            int y = 20 + i * ROW_HEIGHT;
            // 条件名称（左对齐，在勾选框上方）
            guiGraphics.drawString(this.font,
                Component.translatable("gui.reforging_panel.filter_" + filters[i].getSerializedName()),
                6, y + 2, 0x404040, false);
        }

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ========== 辅助方法 ==========

    private boolean isEnabled(int ordinal) {
        return menu.getFilterData().get(ordinal * 2) != 0;
    }

    private Component getCheckboxMessage(int ordinal) {
        return isEnabled(ordinal)
            ? Component.literal("§a☑ ").append(Component.translatable("gui.reforging_panel.participate"))
            : Component.literal("§7☐ ").append(Component.translatable("gui.reforging_panel.not_participate"));
    }

    private String getValueText(int ordinal) {
        int idx = ordinal * 2 + 1;
        int value = menu.getFilterData().get(idx);
        ReforgingFilter filter = ReforgingFilter.values()[ordinal];
        String langKey = ReforgingFilter.getValueDisplayName(filter, value);
        String text = Component.translatable(langKey).getString();

        // 如果是翻译键本身（说明没有翻译），直接显示英文名
        if (text.equals(langKey)) {
            return langKey.replace("gui.reforging_panel.", "");
        }
        return text;
    }

    // ========== 自定义勾选框按钮 ==========

    private static class CheckboxButton extends Button {
        private final java.util.function.BooleanSupplier stateSupplier;

        public CheckboxButton(int x, int y, int width, int height, boolean checked, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
            this.stateSupplier = () -> checked;
            setMessage(getDisplayMessage(checked));
        }

        public void updateState(boolean newChecked) {
            setMessage(getDisplayMessage(newChecked));
        }

        private static Component getDisplayMessage(boolean checked) {
            return checked
                ? Component.literal("§a☑ ").append(Component.translatable("gui.reforging_panel.participate"))
                : Component.literal("§7☐ ").append(Component.translatable("gui.reforging_panel.not_participate"));
        }
    }
}
