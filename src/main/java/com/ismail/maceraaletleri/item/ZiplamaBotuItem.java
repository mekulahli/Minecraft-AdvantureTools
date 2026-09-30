package com.ismail.maceraaletleri.item;

import net.minecraft.world.item.Item;

// Çift zıplama botları: ayağa giyilir. Havadayken zıplama tuşuna tekrar basınca bir kez daha zıplarsın;
// düşme hasarı da yarıya iner. Zıplama istemcide algılanır (client/MaceraAletleriClient), sunucuya paketle bildirilir.
public class ZiplamaBotuItem extends Item {
    public ZiplamaBotuItem(Item.Properties properties) {
        super(properties);
    }
}
