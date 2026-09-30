package com.ismail.maceraaletleri.compat;

import java.util.Optional;
import java.util.function.UnaryOperator;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

// Curios API'sini doğrudan kullanan tek sınıf. Sadece Curios yüklüyse Aksesuarlar üzerinden çağrılır.
final class CuriosUyumu {
    private CuriosUyumu() {
    }

    private static Optional<SlotResult> ara(Player oyuncu, Class<?> esyaSinifi) {
        return CuriosApi.getCuriosInventory(oyuncu)
                .flatMap(envanter -> envanter.findFirstCurio(stack -> esyaSinifi.isInstance(stack.getItem())));
    }

    static ItemStack bul(Player oyuncu, Class<?> esyaSinifi) {
        return ara(oyuncu, esyaSinifi).map(SlotResult::stack).orElse(ItemStack.EMPTY);
    }

    // Curios'ta slottaki eşya doğrudan değiştirilmez; yeni haliyle "takas" edilir (transaction).
    static boolean guncelle(Player oyuncu, Class<?> esyaSinifi, UnaryOperator<ItemStack> degisiklik) {
        Optional<SlotResult> sonuc = ara(oyuncu, esyaSinifi);
        if (sonuc.isEmpty()) {
            return false;
        }
        ItemAccess erisim = sonuc.get().getItemAccess();
        if (erisim == null) {
            return false;
        }

        ItemStack yeni = degisiklik.apply(sonuc.get().stack().copy());
        try (Transaction islem = Transaction.openRoot()) {
            int degisen = yeni.isEmpty()
                    ? erisim.extract(erisim.getResource(), 1, islem)
                    : erisim.exchange(ItemResource.of(yeni), 1, islem);
            if (degisen == 1) {
                islem.commit();
                return true;
            }
        }
        return false;
    }
}
