package com.ismail.maceraaletleri.item;

import java.util.function.Consumer;

import com.ismail.maceraaletleri.ModDataComponents;
import com.ismail.maceraaletleri.menu.SirtCantasiMenu;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

// Sırt çantası. Sağ tık: açar. Shift + sağ tık: sırta takar (göğüs slotu); sırttayken B tuşuyla açılır.
// İçindekiler eşyanın üzerinde (CONTAINER bileşeni) saklanır; çantayı yere atsanız da içindekiler onunla gider.
// Seviyeler (deri → demir → altın → elmas) 3, 4, 5 ve 6 satır. Yükseltme tarifi içindekileri korur.
public class SirtCantasiItem extends Item {
    public static final int OFFHAND_SLOTU = 40;

    private final int satir;

    public SirtCantasiItem(Item.Properties properties, int satir) {
        super(properties);
        this.satir = satir;
    }

    public int getSatir() {
        return satir;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return super.use(level, player, hand); // Equippable: sırta tak
        }
        if (player instanceof ServerPlayer oyuncu) {
            // Çantanın durduğu envanter slotu: menüde bu slot kilitlenir ki çanta açıkken yerinden oynatılamasın.
            int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : OFFHAND_SLOTU;
            ac(oyuncu, player.getItemInHand(hand), slot);
        }
        return InteractionResult.SUCCESS;
    }

    public void ac(ServerPlayer oyuncu, ItemStack canta, int kilitliSlot) {
        oyuncu.openMenu(new SimpleMenuProvider(
                (id, envanter, p) -> new SirtCantasiMenu(id, envanter, new CantaEnvanteri(canta, satir), kilitliSlot, satir),
                canta.getHoverName()), buf -> {
                    buf.writeVarInt(kilitliSlot);
                    buf.writeVarInt(satir);
                });
        oyuncu.level().playSound(null, oyuncu.getX(), oyuncu.getY(), oyuncu.getZ(),
                SoundEvents.ARMOR_EQUIP_LEATHER.value(), SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    // Otomatik toplama: çantada aynı türden eşya varsa yerleştirir. Sığmayan kısmı döndürür.
    public ItemStack otomatikTopla(ItemStack canta, ItemStack esya) {
        CantaEnvanteri envanter = new CantaEnvanteri(canta, satir);
        boolean varMi = envanter.getItems().stream().anyMatch(s -> ItemStack.isSameItemSameComponents(s, esya));
        if (!varMi) {
            return esya;
        }
        ItemStack kalan = envanter.addItem(esya);
        envanter.setChanged();
        return kalan;
    }

    // Envanterde toplama modülünü çantanın üstüne bırakınca modül takılır.
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack canta, ItemStack diger, Slot slot, ClickAction action,
                                            Player player, SlotAccess carriedItem) {
        if (!(diger.getItem() instanceof ToplamaModuluItem)
                || canta.getOrDefault(ModDataComponents.OTOMATIK_TOPLAMA.get(), false)) {
            return false;
        }
        canta.set(ModDataComponents.OTOMATIK_TOPLAMA.get(), true);
        diger.shrink(1);
        player.playSound(SoundEvents.SMITHING_TABLE_USE, 0.8F, 1.2F);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        long dolu = stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).nonEmptyItemCopyStream().count();
        builder.accept(Component.translatable("tooltip.maceraaletleri.canta_doluluk", dolu, satir * 9)
                .withStyle(ChatFormatting.GRAY));
        if (stack.getOrDefault(ModDataComponents.OTOMATIK_TOPLAMA.get(), false)) {
            builder.accept(Component.translatable("tooltip.maceraaletleri.otomatik_toplama").withStyle(ChatFormatting.GREEN));
        }
    }

    // Çantanın içini eşyanın bileşeninden okuyan, her değişiklikte geri yazan envanter.
    public static class CantaEnvanteri extends SimpleContainer {
        private final ItemStack canta;

        public CantaEnvanteri(ItemStack canta, int satir) {
            super(satir * 9);
            this.canta = canta;
            canta.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.getItems());
        }

        @Override
        public void setChanged() {
            super.setChanged();
            canta.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return !(stack.getItem() instanceof SirtCantasiItem);
        }
    }
}
