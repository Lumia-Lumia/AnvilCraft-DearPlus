package dev.anvilcraft.addon.dearplus.client.gui.screen;

import dev.anvilcraft.addon.dearplus.init.ReforgingFilter;
import dev.anvilcraft.addon.dearplus.inventory.ReforgingPanelMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ReforgingPanelScreen extends AbstractContainerScreen<ReforgingPanelMenu> {
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.parse("anvilcraft:textures/gui/backgrounds.png");
    private static final int BG_U = 176;
    private static final int BG_V = 231;
    private static final int BG_W = 176;
    private static final int BG_H = 78;
    private static final int BG_TEX_W = 512;
    private static final int BG_TEX_H = 512;
    private static final ResourceLocation BUTTON_CANCEL =
        ResourceLocation.parse("anvilcraft:textures/gui/machine/cancel.png");
    private static final ResourceLocation BUTTON_CONFIRM =
        ResourceLocation.parse("anvilcraft:textures/gui/machine/confirm.png");

    private static final int GUI_WIDTH = 220;
    private static final int GUI_HEIGHT = 195;
    private static final int ROW_HEIGHT = 23;
    private static final int RIGHT_PAD = 10;
    private static final int LEFT_PAD = 7;
    private static final int CHECKBOX_SIZE = 16;
    private static final int VALUE_W = 64;
    private static final int BTN_H = 16;
    private static final int BG_REL_X = (GUI_WIDTH - BG_W) / 2;
    private static final int BG_REL_Y = (GUI_HEIGHT - BG_H) / 2;
    private static final int TITLE_X = BG_REL_X + LEFT_PAD;
    private static final int CHECKBOX_X = BG_REL_X + BG_W - VALUE_W - RIGHT_PAD - RIGHT_PAD - CHECKBOX_SIZE;
    private static final int VALUE_X = BG_REL_X + BG_W - VALUE_W - RIGHT_PAD;
    private static final int ROW_START_Y = BG_REL_Y + 6;

    private TextButton[] valueButtons;
    private ImageButton[] checkboxButtons;
    private int[] lastCheckboxStates;
    private String[] lastValueTexts;

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
        valueButtons = new TextButton[filters.length];
        checkboxButtons = new ImageButton[filters.length];
        lastCheckboxStates = new int[filters.length];
        lastValueTexts = new String[filters.length];
        var filterData = menu.getFilterData();

        for (int i = 0; i < filters.length; i++) {
            int y = top + ROW_START_Y + i * ROW_HEIGHT;
            int ordinal = i;
            ReforgingFilter filter = filters[i];

            // 参与/不参与筛选按钮（图片按钮，垂直居中于行）
            int cbY = y + (BTN_H - CHECKBOX_SIZE) / 2;
            checkboxButtons[i] = new ImageButton(
                left + CHECKBOX_X, cbY, CHECKBOX_SIZE, CHECKBOX_SIZE,
                isEnabled(ordinal) ? BUTTON_CONFIRM : BUTTON_CANCEL,
                btn -> {
                    int idx = ordinal * 2;
                    filterData.set(idx, filterData.get(idx) == 0 ? 1 : 0);
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ordinal);
                }
            );
            addRenderableWidget(checkboxButtons[i]);

            // 值循环按钮（材质背景+文本）
            valueButtons[i] = new TextButton(
                left + VALUE_X, y, VALUE_W, BTN_H,
                getValueText(ordinal),
                btn -> {
                    int idx = ordinal * 2 + 1;
                    int cur = filterData.get(idx);
                    int max = filter.getMaxValue();
                    filterData.set(idx, (cur + 1) % (max + 1));
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 10 + ordinal);
                }
            );
            addRenderableWidget(valueButtons[i]);
        }

        // 配置在每次点击筛选按钮时自动保存，按E/Esc退出即可
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        for (int i = 0; i < valueButtons.length; i++) {
            // 仅当勾选状态变化时更新复选框纹理
            int curEnabled = isEnabled(i) ? 1 : 0;
            if (checkboxButtons[i] != null && lastCheckboxStates[i] != curEnabled) {
                lastCheckboxStates[i] = curEnabled;
                checkboxButtons[i].updateTexture(curEnabled == 1 ? BUTTON_CONFIRM : BUTTON_CANCEL);
            }
            // 仅当值文本变化时更新按钮文本
            if (valueButtons[i] != null) {
                String curText = getValueText(i);
                if (!curText.equals(lastValueTexts[i])) {
                    lastValueTexts[i] = curText;
                    valueButtons[i].updateText(curText);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) {
            for (int i = 0; i < valueButtons.length; i++) {
                if (valueButtons[i] != null && valueButtons[i].isMouseOver(mouseX, mouseY)) {
                    int idx = i * 2 + 1;
                    int cur = menu.getFilterData().get(idx);
                    int max = ReforgingFilter.values()[i].getMaxValue();
                    menu.getFilterData().set(idx, (cur - 1 + max + 1) % (max + 1));
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
                    menu.getFilterData().set(idx, (cur + 1) % (max + 1));
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 10 + i);
                } else {
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
        int bgLeft = leftPos + (GUI_WIDTH - BG_W) / 2;
        int bgTop = topPos + (GUI_HEIGHT - BG_H) / 2;
        guiGraphics.blit(BACKGROUND, bgLeft, bgTop, BG_U, BG_V, BG_W, BG_H, BG_TEX_W, BG_TEX_H);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ReforgingFilter[] filters = ReforgingFilter.values();
        int titleAreaWidth = CHECKBOX_X - TITLE_X;
        for (int i = 0; i < filters.length; i++) {
            int ty = ROW_START_Y + i * ROW_HEIGHT + (BTN_H - 8) / 2;
            Component text = Component.translatable("gui.reforging_panel.filter_" + filters[i].getSerializedName());
            int textW = this.font.width(text);
            int tx = TITLE_X + (titleAreaWidth - textW) / 2;
            guiGraphics.drawString(this.font, text, tx, ty, 0xFFFFFF, false);
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

    private String getValueText(int ordinal) {
        int idx = ordinal * 2 + 1;
        int value = menu.getFilterData().get(idx);
        ReforgingFilter filter = ReforgingFilter.values()[ordinal];
        String langKey = ReforgingFilter.getValueDisplayName(filter, value);
        String text = Component.translatable(langKey).getString();
        if (text.equals(langKey)) {
            return langKey.replace("gui.reforging_panel.", "");
        }
        return text;
    }

    // ========== 材质背景文本按钮 ==========

    private static class TextButton extends Button {
        private Component text;
        private static final ResourceLocation TEXTURE =
            ResourceLocation.parse("anvilcraft_dearplus:textures/gui/button_cycle.png");

        public TextButton(int x, int y, int width, int height, String text, OnPress onPress) {
            super(x, y, width, height, Component.literal(text), onPress, DEFAULT_NARRATION);
            this.text = Component.literal(text);
        }

        public void updateText(String newText) {
            this.text = Component.literal(newText);
            setMessage(this.text);
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 材质上下分半：上半＝默认，下半＝悬停（仅鼠标悬停）
            int vOffset = isHovered() ? height : 0;
            guiGraphics.blit(TEXTURE, getX(), getY(), 0, vOffset, width, height, width, height * 2);
            // 在材质之上居中绘制文本
            var font = net.minecraft.client.Minecraft.getInstance().font;
            int textW = font.width(text);
            int color = isHovered() ? 0xFFFFA0 : 0xFFFFFF;
            guiGraphics.drawString(font, text,
                getX() + (width - textW) / 2,
                getY() + (height - 8) / 2 - 1, color, false);
        }
    }

    // ========== 图片按钮 ==========

    private static class ImageButton extends Button {
        private ResourceLocation texture;

        public ImageButton(int x, int y, int width, int height, ResourceLocation texture, OnPress onPress) {
            super(x, y, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
            this.texture = texture;
        }

        public void updateTexture(ResourceLocation newTexture) {
            this.texture = newTexture;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 材质上下分半：上半＝默认，下半＝悬停（仅鼠标悬停）
            int vOffset = isHovered() ? height : 0;
            guiGraphics.blit(texture, getX(), getY(), 0, vOffset, width, height, width, height * 2);
        }
    }
}
