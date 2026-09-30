package com.ismail.maceraaletleri;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

// Modun giriş noktası. Buradaki değer neoforge.mods.toml içindeki modId ile aynı olmalı.
@Mod(MaceraAletleri.MODID)
public class MaceraAletleri {
    public static final String MODID = "maceraaletleri";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Mod yüklenirken ilk çalışan kod. NeoForge, IEventBus ve ModContainer'ı otomatik verir.
    public MaceraAletleri(IEventBus modEventBus, ModContainer modContainer) {
        ModDataComponents.COMPONENTS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, Ayarlar.SPEC);

        LOGGER.info("Macera Aletleri yukleniyor!");
    }
}
