package com.ismail.maceraaletleri.menu;

import com.ismail.maceraaletleri.ModMenus;
import com.ismail.maceraaletleri.item.SirtCantasiItem;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// Sırt çantası menüsü: üstte çanta (seviyeye göre 3-6 satır), altta oyuncunun envanteri. Vanilla ChestMenu'dan uyarlandı.
// İki güvenlik önlemi var:
//  - Çanta çantanın içine konamaz.
//  - Açık olan çantanın durduğu slot kilitlenir; yoksa çantayı taşıyıp eşya kopyalamak mümkün olurdu.
public class SirtCantasiMenu extends AbstractContainerMenu {
    private final Container canta;
    private final int kilitliSlot;
    private final int satir;
    private final int boyut;

    // Sunucu tarafı: gerçek çanta envanteriyle açılır.
    public SirtCantasiMenu(int id, Inventory envanter, Container canta, int kilitliSlot, int satir) {
        super(ModMenus.SIRT_CANTASI.get(), id);
        this.canta = canta;
        this.kilitliSlot = kilitliSlot;
        this.satir = satir;
        this.boyut = satir * 9;

        for (int y = 0; y < satir; y++) {
            for (int x = 0; x < 9; x++) {
                this.addSlot(new CantaSlotu(canta, x + y * 9, 8 + x * 18, 18 + y * 18));
            }
        }

        int envanterUst = 18 + satir * 18 + 13;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                oyuncuSlotuEkle(envanter, x + y * 9 + 9, 8 + x * 18, envanterUst + y * 18);
            }
        }
        for (int x = 0; x < 9; x++) {
            oyuncuSlotuEkle(envanter, x, 8 + x * 18, envanterUst + 58);
        }
    }

    // İstemci tarafı: içerik sunucudan gelir; kilitli slot ve satır sayısı paketle gönderilir.
    public SirtCantasiMenu(int id, Inventory envanter, RegistryFriendlyByteBuf buf) {
        this(id, envanter, buf.readVarInt(), buf.readVarInt());
    }

    private SirtCantasiMenu(int id, Inventory envanter, int kilitliSlot, int satir) {
        this(id, envanter, new SimpleContainer(satir * 9), kilitliSlot, satir);
    }

    public int getSatir() {
        return satir;
    }

    private void oyuncuSlotuEkle(Inventory envanter, int index, int x, int y) {
        if (index == kilitliSlot) {
            this.addSlot(new KilitliSlot(envanter, index, x, y));
        } else {
            this.addSlot(new Slot(envanter, index, x, y));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getInventory().getItem(kilitliSlot).getItem() instanceof SirtCantasiItem;
    }

    // Sayı tuşları (1-9) ya da F tuşuyla kilitli slottaki çantayla yer değiştirmeyi engelle.
    @Override
    public void clicked(int slotIndex, int buttonNum, ContainerInput input, Player player) {
        if (input == ContainerInput.SWAP && buttonNum == kilitliSlot) {
            return;
        }
        super.clicked(slotIndex, buttonNum, input, player);
    }

    // Shift+tık: çantadan envantere ya da envanterden çantaya taşı.
    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack sonuc = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            sonuc = stack.copy();
            if (slotIndex < boyut) {
                if (!this.moveItemStackTo(stack, boyut, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, boyut, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return sonuc;
    }

    private static class CantaSlotu extends Slot {
        CantaSlotu(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return !(stack.getItem() instanceof SirtCantasiItem);
        }
    }

    private static class KilitliSlot extends Slot {
        KilitliSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
