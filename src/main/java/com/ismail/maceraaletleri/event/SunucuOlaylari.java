package com.ismail.maceraaletleri.event;

import java.util.ArrayList;
import java.util.List;

import com.ismail.maceraaletleri.Ayarlar;
import com.ismail.maceraaletleri.MaceraAletleri;
import com.ismail.maceraaletleri.ModDataComponents;
import com.ismail.maceraaletleri.ModItems;
import com.ismail.maceraaletleri.block.UykuTulumuBlock;
import com.ismail.maceraaletleri.item.KasifDurbunuItem;
import com.ismail.maceraaletleri.item.SirtCantasiItem;
import com.ismail.maceraaletleri.item.ZiplamaBotuItem;
import com.ismail.maceraaletleri.menu.SirtCantasiMenu;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.util.TriState;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

// Sunucu tarafı oyun olayları.
@EventBusSubscriber(modid = MaceraAletleri.MODID)
public final class SunucuOlaylari {
    private static final String REHBER_VERILDI = MaceraAletleri.MODID + ":rehber_verildi";
    private static final int REHBER_SAYFA_SAYISI = 8;

    private SunucuOlaylari() {
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        KasifDurbunuItem.parlamalariTemizle();
    }

    @SubscribeEvent
    static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()
                && event.getEntity() instanceof Display.BlockDisplay gorunum
                && KasifDurbunuItem.artikParlamaMi(gorunum)) {
            event.setCanceled(true);
        }
    }

    // --- Çift zıplama botları: düşme hasarı yarıya iner ---
    @SubscribeEvent
    static void onFall(LivingFallEvent event) {
        if (event.getEntity().getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ZiplamaBotuItem) {
            event.setDamageMultiplier(event.getDamageMultiplier() * 0.5F);
        }
    }

    // --- Uyku tulumu: uyumak doğma noktasını değiştirmesin ---
    @SubscribeEvent
    static void onSetSpawn(PlayerSetSpawnEvent event) {
        if (event.getNewSpawn() != null
                && event.getEntity().level().getBlockState(event.getNewSpawn()).getBlock() instanceof UykuTulumuBlock) {
            event.setCanceled(true);
        }
    }

    // --- Sırt çantası otomatik toplama: çantada zaten olan türden eşyalar doğrudan çantaya gider ---
    @SubscribeEvent
    static void onPickup(ItemEntityPickupEvent.Pre event) {
        Player oyuncu = event.getPlayer();
        ItemEntity esya = event.getItemEntity();
        // Çanta penceresi açıkken çantaya dışarıdan yazmak, penceredeki içerikle çakışırdı.
        if (oyuncu.level().isClientSide() || oyuncu.containerMenu instanceof SirtCantasiMenu || esya.hasPickUpDelay()) {
            return;
        }

        List<ItemStack> cantalar = new ArrayList<>();
        cantalar.add(oyuncu.getItemBySlot(EquipmentSlot.CHEST));
        for (int i = 0; i < oyuncu.getInventory().getContainerSize(); i++) {
            cantalar.add(oyuncu.getInventory().getItem(i));
        }

        ItemStack kalan = esya.getItem().copy();
        for (ItemStack canta : cantalar) {
            if (kalan.isEmpty()) {
                break;
            }
            if (canta.getItem() instanceof SirtCantasiItem cantaItem
                    && canta.getOrDefault(ModDataComponents.OTOMATIK_TOPLAMA.get(), false)) {
                kalan = cantaItem.otomatikTopla(canta, kalan);
            }
        }

        if (kalan.getCount() != esya.getItem().getCount()) {
            oyuncu.take(esya, esya.getItem().getCount() - kalan.getCount());
            oyuncu.level().playSound(null, oyuncu.getX(), oyuncu.getY(), oyuncu.getZ(),
                    SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.5F, 1.0F);
            if (kalan.isEmpty()) {
                esya.discard();
                event.setCanPickup(TriState.FALSE);
            } else {
                esya.setItem(kalan);
            }
        }
    }

    // --- Ölüm pusulası: yeniden doğunca eşyaların bırakıldığı yeri gösteren pusula ---
    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered() || !Ayarlar.OLUM_PUSULASI.get()
                || !(event.getEntity() instanceof ServerPlayer oyuncu)
                || ((ServerLevel) oyuncu.level()).getGameRules().get(GameRules.KEEP_INVENTORY)) {
            return;
        }
        GlobalPos olumYeri = oyuncu.getLastDeathLocation().orElse(null);
        if (olumYeri == null) {
            return;
        }
        ItemStack pusula = new ItemStack(ModItems.OLUM_PUSULASI.get());
        pusula.set(ModDataComponents.KAYITLI_KONUM.get(), olumYeri);
        if (!oyuncu.getInventory().add(pusula)) {
            oyuncu.drop(pusula, false);
        }
    }

    // --- Macera rehberi: oyuna ilk girişte bir kez verilir ---
    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player oyuncu = event.getEntity();
        if (!Ayarlar.REHBER_KITAP.get() || oyuncu.getPersistentData().getBooleanOr(REHBER_VERILDI, false)) {
            return;
        }
        oyuncu.getPersistentData().putBoolean(REHBER_VERILDI, true);
        if (!oyuncu.getInventory().add(rehberKitabi())) {
            oyuncu.drop(rehberKitabi(), false);
        }
    }

    public static ItemStack rehberKitabi() {
        List<Filterable<Component>> sayfalar = new ArrayList<>();
        for (int i = 1; i <= REHBER_SAYFA_SAYISI; i++) {
            sayfalar.add(Filterable.passThrough(Component.translatable("rehber.maceraaletleri.sayfa." + i)));
        }
        ItemStack kitap = new ItemStack(Items.WRITTEN_BOOK);
        kitap.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(
                Filterable.passThrough("Macera Rehberi"), "Macera Aletleri", 0, sayfalar, true));
        return kitap;
    }
}
