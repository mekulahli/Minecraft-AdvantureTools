package com.ismail.maceraaletleri;

import com.mojang.serialization.Codec;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Eşyaların üzerinde saklanan modumuza özel veriler (ItemStack bileşenleri).
public final class ModDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MaceraAletleri.MODID);

    // Mıknatıs yüzüğü açık mı?
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> MIKNATIS_ACIK =
            COMPONENTS.registerComponentType("miknatis_acik", b -> b
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    // Işınlanma pusulasına kaydedilen konum (boyut + blok).
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> KAYITLI_KONUM =
            COMPONENTS.registerComponentType("kayitli_konum", b -> b
                    .persistent(GlobalPos.CODEC)
                    .networkSynchronized(GlobalPos.STREAM_CODEC));

    // Zipline makarasıyla seçilen ilk nokta.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> ZIPLINE_BASLANGIC =
            COMPONENTS.registerComponentType("zipline_baslangic", b -> b
                    .persistent(GlobalPos.CODEC)
                    .networkSynchronized(GlobalPos.STREAM_CODEC));

    // 3D manevra takımının kalan gazı.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> GAZ =
            COMPONENTS.registerComponentType("gaz", b -> b
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    // Sırt çantasına takılan otomatik toplama modülü.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> OTOMATIK_TOPLAMA =
            COMPONENTS.registerComponentType("otomatik_toplama", b -> b
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    private ModDataComponents() {
    }
}
