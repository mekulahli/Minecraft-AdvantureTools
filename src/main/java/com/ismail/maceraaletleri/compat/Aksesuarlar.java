package com.ismail.maceraaletleri.compat;

import java.util.function.UnaryOperator;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

// Aksesuar slotlarına (Curios modu) erişim. Curios kurulu değilse her şey boş döner.
// Curios sınıflarına dokunan kod ayrı bir sınıfta (CuriosUyumu); Curios yoksa o sınıf hiç yüklenmez.
public final class Aksesuarlar {
    private static final boolean CURIOS_YUKLU = ModList.get().isLoaded("curios");

    private Aksesuarlar() {
    }

    // Oyuncunun aksesuar slotlarında bu türden bir eşya varsa onu döndürür (salt okunur kopya olabilir).
    public static ItemStack bul(Player oyuncu, Class<?> esyaSinifi) {
        return CURIOS_YUKLU ? CuriosUyumu.bul(oyuncu, esyaSinifi) : ItemStack.EMPTY;
    }

    // Aksesuar slotundaki eşyayı değiştirir (ör. dayanıklılık düşürme, aç/kapat). Bulunduysa true.
    public static boolean guncelle(Player oyuncu, Class<?> esyaSinifi, UnaryOperator<ItemStack> degisiklik) {
        return CURIOS_YUKLU && CuriosUyumu.guncelle(oyuncu, esyaSinifi, degisiklik);
    }
}
