package com.ismail.maceraaletleri;

import com.ismail.maceraaletleri.item.GazTupuItem;
import com.ismail.maceraaletleri.item.IpMerdivenItem;
import com.ismail.maceraaletleri.item.IsaretFisegiItem;
import com.ismail.maceraaletleri.item.IsinlanmaPusulasiItem;
import com.ismail.maceraaletleri.item.KancaItem;
import com.ismail.maceraaletleri.item.KancaSeviyesi;
import com.ismail.maceraaletleri.item.KasifDurbunuItem;
import com.ismail.maceraaletleri.item.ManevraTakimiItem;
import com.ismail.maceraaletleri.item.MiknatisYuzuguItem;
import com.ismail.maceraaletleri.item.OlumPusulasiItem;
import com.ismail.maceraaletleri.item.PlanorItem;
import com.ismail.maceraaletleri.item.SirtCantasiItem;
import com.ismail.maceraaletleri.item.TirmanmaEldiveniItem;
import com.ismail.maceraaletleri.item.ToplamaModuluItem;
import com.ismail.maceraaletleri.item.ZiplamaBotuItem;
import com.ismail.maceraaletleri.item.ZiplineMakarasiItem;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// Modun tüm eşyaları burada kaydedilir.
// durability(): dayanıklılık (yığını 1'e indirir). repairable(): örste tamir malzemesi.
// enchantable(): büyü masasında büyülenebilirlik (sayı büyüdükçe daha iyi büyüler gelir).
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MaceraAletleri.MODID);

    // --- Kancalar ---
    public static final DeferredItem<KancaItem> DEMIR_KANCA = ITEMS.registerItem("demir_kanca",
            p -> new KancaItem(p, KancaSeviyesi.DEMIR),
            p -> p.durability(KancaSeviyesi.DEMIR.dayaniklilik()).repairable(Items.IRON_INGOT).enchantable(14));

    public static final DeferredItem<KancaItem> ELMAS_KANCA = ITEMS.registerItem("elmas_kanca",
            p -> new KancaItem(p, KancaSeviyesi.ELMAS),
            p -> p.durability(KancaSeviyesi.ELMAS.dayaniklilik()).repairable(Items.DIAMOND).enchantable(10));

    // Netherite kanca lavda yanmaz, tıpkı netherite aletler gibi.
    public static final DeferredItem<KancaItem> NETHERITE_KANCA = ITEMS.registerItem("netherite_kanca",
            p -> new KancaItem(p, KancaSeviyesi.NETHERITE),
            p -> p.durability(KancaSeviyesi.NETHERITE.dayaniklilik()).repairable(Items.NETHERITE_INGOT)
                    .fireResistant().enchantable(15));

    // 3D manevra takımı: yeni yapılan takımın deposu dolu gelir.
    public static final DeferredItem<ManevraTakimiItem> MANEVRA_TAKIMI = ITEMS.registerItem("manevra_takimi",
            ManevraTakimiItem::new, p -> p.stacksTo(1).rarity(Rarity.RARE)
                    .component(ModDataComponents.GAZ.get(), ManevraTakimiItem.GAZ_MAKS));

    public static final DeferredItem<GazTupuItem> GAZ_TUPU = ITEMS.registerItem("gaz_tupu",
            GazTupuItem::new, p -> p.stacksTo(16));

    // --- Hareket ---
    public static final DeferredItem<PlanorItem> PLANOR = ITEMS.registerItem("planor",
            PlanorItem::new, p -> p.durability(256).repairable(Items.LEATHER).enchantable(12));

    public static final DeferredItem<ZiplamaBotuItem> ZIPLAMA_BOTU = ITEMS.registerItem("ziplama_botu",
            ZiplamaBotuItem::new, p -> p.durability(320).repairable(Items.PHANTOM_MEMBRANE).enchantable(12)
                    .rarity(Rarity.UNCOMMON)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.FEET)
                            .setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER)
                            .setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID,
                                    Identifier.fromNamespaceAndPath(MaceraAletleri.MODID, "ziplama_botu")))
                            .build()));

    public static final DeferredItem<TirmanmaEldiveniItem> TIRMANMA_ELDIVENI = ITEMS.registerItem("tirmanma_eldiveni",
            TirmanmaEldiveniItem::new, p -> p.stacksTo(1));

    public static final DeferredItem<ZiplineMakarasiItem> ZIPLINE_MAKARASI = ITEMS.registerItem("zipline_makarasi",
            ZiplineMakarasiItem::new, p -> p.stacksTo(16));

    public static final DeferredItem<IpMerdivenItem> IP_MERDIVEN = ITEMS.registerItem("ip_merdiven",
            IpMerdivenItem::new, p -> p.stacksTo(16));

    // --- Sırt çantaları (sağ tık: aç, Shift + sağ tık: sırta tak) ---
    public static final DeferredItem<SirtCantasiItem> SIRT_CANTASI = canta("sirt_cantasi", 3);
    public static final DeferredItem<SirtCantasiItem> DEMIR_SIRT_CANTASI = canta("demir_sirt_cantasi", 4);
    public static final DeferredItem<SirtCantasiItem> ALTIN_SIRT_CANTASI = canta("altin_sirt_cantasi", 5);
    public static final DeferredItem<SirtCantasiItem> ELMAS_SIRT_CANTASI = canta("elmas_sirt_cantasi", 6);

    public static final DeferredItem<ToplamaModuluItem> TOPLAMA_MODULU = ITEMS.registerItem("toplama_modulu",
            ToplamaModuluItem::new, p -> p.stacksTo(1).rarity(Rarity.UNCOMMON));

    // --- Keşif ---
    public static final DeferredItem<MiknatisYuzuguItem> MIKNATIS_YUZUGU = ITEMS.registerItem("miknatis_yuzugu",
            MiknatisYuzuguItem::new, p -> p.stacksTo(1).rarity(Rarity.UNCOMMON).enchantable(15));

    public static final DeferredItem<IsinlanmaPusulasiItem> ISINLANMA_PUSULASI = ITEMS.registerItem("isinlanma_pusulasi",
            IsinlanmaPusulasiItem::new, p -> p.durability(64).repairable(Items.ENDER_PEARL).rarity(Rarity.RARE).enchantable(10));

    public static final DeferredItem<KasifDurbunuItem> KASIF_DURBUNU = ITEMS.registerItem("kasif_durbunu",
            KasifDurbunuItem::new, p -> p.durability(128).repairable(Items.GOLD_INGOT).rarity(Rarity.UNCOMMON).enchantable(10));

    public static final DeferredItem<IsaretFisegiItem> ISARET_FISEGI = ITEMS.registerItem("isaret_fisegi",
            IsaretFisegiItem::new, p -> p.stacksTo(16));

    public static final DeferredItem<OlumPusulasiItem> OLUM_PUSULASI = ITEMS.registerItem("olum_pusulasi",
            OlumPusulasiItem::new, p -> p.stacksTo(1));

    public static final DeferredItem<?> UYKU_TULUMU = ITEMS.registerSimpleBlockItem("uyku_tulumu", ModBlocks.UYKU_TULUMU);

    private ModItems() {
    }

    private static DeferredItem<SirtCantasiItem> canta(String ad, int satir) {
        return ITEMS.registerItem(ad, p -> new SirtCantasiItem(p, satir), p -> p.stacksTo(1)
                .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
                        .setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER).build()));
    }
}
