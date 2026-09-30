package com.ismail.maceraaletleri;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Kreatif menüde "Macera Aletleri" sekmesi. ModItems'a eklenen her eşya otomatik olarak burada görünür.
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MaceraAletleri.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MACERA_TAB =
            CREATIVE_MODE_TABS.register("macera_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MaceraAletleri.MODID))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.DEMIR_KANCA.get().getDefaultInstance())
                    .displayItems((parameters, output) ->
                            ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build());

    private ModCreativeTabs() {
    }
}
