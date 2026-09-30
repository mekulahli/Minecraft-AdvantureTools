package com.ismail.maceraaletleri;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

// Koddan verilen başarımlar. JSON tarafında bu başarımlar "minecraft:impossible" kriteri kullanır,
// yani oyunun kendisi vermez; biz uygun anda buradan veririz.
public final class Basarimlar {
    public static final String TARZAN = "tarzan";
    public static final String GEL_BURAYA = "gel_buraya";
    public static final String KUS_GIBI = "kus_gibi";
    public static final String ZIPLINE = "zipline";
    public static final String HAZINE_AVCISI = "hazine_avcisi";
    public static final String TITAN_AVCISI = "titan_avcisi";
    public static final String SICAK_HAVA = "sicak_hava";
    public static final String CIFT_ZIPLAMA = "cift_ziplama";

    private static final String KRITER = "kod";

    private Basarimlar() {
    }

    public static void ver(ServerPlayer oyuncu, String ad) {
        Identifier id = Identifier.fromNamespaceAndPath(MaceraAletleri.MODID, "macera/" + ad);
        AdvancementHolder basarim = oyuncu.level().getServer().getAdvancements().get(id);
        if (basarim != null) {
            oyuncu.getAdvancements().award(basarim, KRITER);
        }
    }
}
