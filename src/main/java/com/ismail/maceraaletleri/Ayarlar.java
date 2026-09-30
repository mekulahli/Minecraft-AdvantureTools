package com.ismail.maceraaletleri;

import net.neoforged.neoforge.common.ModConfigSpec;

// Sunucu ayarları: world/serverconfig/maceraaletleri-server.toml dosyasına yazılır ve oyunculara otomatik gönderilir.
// Oyun içinde: Mods → Macera Aletleri → Config.
public final class Ayarlar {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    static {
        BUILDER.push("kanca");
    }

    public static final ModConfigSpec.DoubleValue KANCA_MENZIL_CARPANI = BUILDER
            .comment("Tüm kancaların menzil çarpanı (1.0 = varsayılan)")
            .defineInRange("menzilCarpani", 1.0, 0.1, 5.0);

    public static final ModConfigSpec.DoubleValue KANCA_HIZ_CARPANI = BUILDER
            .comment("Tüm kancaların çekme hızı çarpanı (1.0 = varsayılan)")
            .defineInRange("hizCarpani", 1.0, 0.1, 5.0);

    public static final ModConfigSpec.BooleanValue KANCA_MOB_CEKME = BUILDER
            .comment("Kanca mobları ve yerdeki eşyaları çekebilsin mi")
            .define("mobCekme", true);

    static {
        BUILDER.pop().push("planor");
    }

    public static final ModConfigSpec.DoubleValue PLANOR_DUSME_HIZI = BUILDER
            .comment("Planörle en fazla düşme hızı (blok/tick). Küçüldükçe daha yavaş düşülür.")
            .defineInRange("dusmeHizi", 0.08, 0.01, 1.0);

    public static final ModConfigSpec.DoubleValue PLANOR_ILERI_HIZ = BUILDER
            .comment("Planörle ileri süzülme hızı (blok/tick)")
            .defineInRange("ileriHiz", 0.4, 0.05, 2.0);

    static {
        BUILDER.pop().push("miknatis");
    }

    public static final ModConfigSpec.IntValue MIKNATIS_YARICAP = BUILDER
            .comment("Mıknatıs yüzüğünün eşya çekme yarıçapı (blok)")
            .defineInRange("yaricap", 8, 1, 32);

    static {
        BUILDER.pop().push("pusula");
    }

    public static final ModConfigSpec.IntValue PUSULA_BEKLEME = BUILDER
            .comment("Işınlanma pusulasının bekleme süresi (saniye)")
            .defineInRange("beklemeSuresi", 30, 0, 3600);

    public static final ModConfigSpec.BooleanValue PUSULA_BOYUTLAR_ARASI = BUILDER
            .comment("Pusula başka boyuta (Nether, End) ışınlayabilsin mi")
            .define("boyutlarArasi", true);

    static {
        BUILDER.pop().push("durbun");
    }

    public static final ModConfigSpec.IntValue DURBUN_YARICAP = BUILDER
            .comment("Kaşif dürbününün cevher arama yarıçapı (blok)")
            .defineInRange("yaricap", 12, 4, 32);

    public static final ModConfigSpec.IntValue DURBUN_SURE = BUILDER
            .comment("Bulunan cevherlerin parlama süresi (saniye)")
            .defineInRange("parlamaSuresi", 10, 1, 60);

    static {
        BUILDER.pop().push("zipline");
    }

    public static final ModConfigSpec.IntValue ZIPLINE_UZUNLUK = BUILDER
            .comment("Bir zipline hattının en fazla uzunluğu (blok)")
            .defineInRange("maksUzunluk", 48, 4, 128);

    static {
        BUILDER.pop().push("manevra");
    }

    public static final ModConfigSpec.DoubleValue MANEVRA_IVME = BUILDER
            .comment("3D manevra takımının çekme ivmesi (blok/tick²)")
            .defineInRange("ivme", 0.09, 0.01, 0.5);

    public static final ModConfigSpec.IntValue MANEVRA_GAZ_TUKETIMI = BUILDER
            .comment("Takılı kanca başına tick'te harcanan gaz (depo: 1000)")
            .defineInRange("gazTuketimi", 1, 0, 50);

    static {
        BUILDER.pop().push("genel");
    }

    public static final ModConfigSpec.BooleanValue OLUM_PUSULASI = BUILDER
            .comment("Ölünce yeniden doğan oyuncuya eşyalarını gösteren bir ölüm pusulası verilsin mi")
            .define("olumPusulasi", true);

    public static final ModConfigSpec.BooleanValue REHBER_KITAP = BUILDER
            .comment("Oyuna ilk girişte Macera Rehberi kitabı verilsin mi")
            .define("rehberKitap", true);

    static {
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();

    private Ayarlar() {
    }
}
