package com.ismail.maceraaletleri;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

// Modun büyüleri. Büyülerin kendisi JSON (data/maceraaletleri/enchantment); etkileri kodda okunur.
public final class Buyuler {
    public static final ResourceKey<Enchantment> MENZIL = anahtar("menzil");
    public static final ResourceKey<Enchantment> HIZLI_CEKIM = anahtar("hizli_cekim");
    public static final ResourceKey<Enchantment> SUZULME = anahtar("suzulme");
    public static final ResourceKey<Enchantment> CEKIM_GUCU = anahtar("cekim_gucu");

    private Buyuler() {
    }

    private static ResourceKey<Enchantment> anahtar(String ad) {
        return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(MaceraAletleri.MODID, ad));
    }

    // Eşyanın üzerindeki büyü seviyesi (yoksa 0).
    public static int seviye(Level level, ResourceKey<Enchantment> buyu, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(buyu)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack))
                .orElse(0);
    }
}
