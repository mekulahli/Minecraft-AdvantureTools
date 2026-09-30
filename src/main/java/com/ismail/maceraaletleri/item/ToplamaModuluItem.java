package com.ismail.maceraaletleri.item;

import net.minecraft.world.item.Item;

// Sırt çantasına takılan yükseltme: envanterde modülü çantanın üstüne bırakınca takılır.
// Takılı çanta, içinde zaten bulunan türden eşyaları yerden doğrudan kendine alır.
public class ToplamaModuluItem extends Item {
    public ToplamaModuluItem(Item.Properties properties) {
        super(properties);
    }
}
