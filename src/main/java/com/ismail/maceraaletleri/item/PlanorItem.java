package com.ismail.maceraaletleri.item;

import net.minecraft.world.item.Item;

// Elde (ana el ya da yan el) tutulurken düşüş sırasında açılır ve süzülmeyi sağlar.
// Asıl davranış event/HareketOlaylari içinde; bu sınıf sadece eşyayı tanımlar.
public class PlanorItem extends Item {
    public PlanorItem(Item.Properties properties) {
        super(properties);
    }
}
