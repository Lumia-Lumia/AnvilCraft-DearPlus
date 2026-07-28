package dev.anvilcraft.addon.dearpluscelestialreforge.data.lang;

import dev.anvilcraft.addon.dearpluscelestialreforge.AddonConfig;
import dev.anvilcraft.lib.v2.config.ConfigData;
import dev.anvilcraft.lib.v2.registrum.providers.RegistrumLangProvider;

public class AddonLangHandler {

    /**
     * 语言文件初始化
     *
     * @param provider 提供器
     */
    public static void init(RegistrumLangProvider provider) {
        ConfigData.readConfigClass(provider, AddonConfig.class);

        // 重锻面板 GUI 文本
        provider.add("container.anvilcraft_dearpluscelestialreforge.reforging_panel", "重锻面板");

        // 筛选条件名称
        provider.add("gui.reforging_panel.filter_magnetic_field", "磁场");
        provider.add("gui.reforging_panel.filter_rotation_speed", "转速");
        provider.add("gui.reforging_panel.filter_temperature", "温度");
        provider.add("gui.reforging_panel.filter_fluid_coverage", "流体覆盖率");
        provider.add("gui.reforging_panel.filter_resource", "行星特殊资源");

        // 磁场选项
        provider.add("gui.reforging_panel.magnetic.none", "无");
        provider.add("gui.reforging_panel.magnetic.barely", "几乎没有");
        provider.add("gui.reforging_panel.magnetic.very_weak", "非常弱");
        provider.add("gui.reforging_panel.magnetic.weak", "弱");
        provider.add("gui.reforging_panel.magnetic.medium", "中等");
        provider.add("gui.reforging_panel.magnetic.strong", "强");
        provider.add("gui.reforging_panel.magnetic.very_strong", "非常强");

        // 转速选项
        provider.add("gui.reforging_panel.speed.very_slow", "非常慢");
        provider.add("gui.reforging_panel.speed.slow", "慢");
        provider.add("gui.reforging_panel.speed.medium", "中等");
        provider.add("gui.reforging_panel.speed.fast", "快");
        provider.add("gui.reforging_panel.speed.very_fast", "非常快");

        // 温度选项
        provider.add("gui.reforging_panel.temp.freezing", "极寒");
        provider.add("gui.reforging_panel.temp.cold", "寒冷");
        provider.add("gui.reforging_panel.temp.mild", "温和");
        provider.add("gui.reforging_panel.temp.hot", "炎热");
        provider.add("gui.reforging_panel.temp.scorched", "焦土");

        // 流体覆盖率选项
        provider.add("gui.reforging_panel.fluid.none", "无");
        provider.add("gui.reforging_panel.fluid.low", "低");
        provider.add("gui.reforging_panel.fluid.medium", "中等");
        provider.add("gui.reforging_panel.fluid.high", "高");

        // 资源选项
        provider.add("gui.reforging_panel.resource.none", "无");
        provider.add("gui.reforging_panel.resource.biological", "生物资源");
        provider.add("gui.reforging_panel.resource.civilization", "文明资源");
        provider.add("gui.reforging_panel.resource.any", "任意资源");

        // 其他
        provider.add("gui.reforging_panel.save", "保存");
        provider.add("gui.reforging_panel.emitting", "§c■ 信号输出中");
        provider.add("gui.reforging_panel.idle", "§7■ 待机");
        provider.add("gui.reforging_panel.participate", "参与筛选");
        provider.add("gui.reforging_panel.not_participate", "不参与");
    }
}
