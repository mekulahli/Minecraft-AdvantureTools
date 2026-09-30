package com.ismail.maceraaletleri.item;

import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.ismail.maceraaletleri.ModDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

// Ölünce yeniden doğan oyuncuya verilir (SunucuOlaylari#onRespawn). İğnesi vanilla "recovery compass"
// modelleriyle son ölüm yerini gösterir. Eldeyken kalan mesafeyi yazar; ölüm yerine varınca kendiliğinden kaybolur.
public class OlumPusulasiItem extends Item {
    private static final double VARIS_MESAFESI = 4.0;

    public OlumPusulasiItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        GlobalPos olumYeri = stack.get(ModDataComponents.KAYITLI_KONUM.get());
        if (olumYeri == null || !(owner instanceof Player oyuncu) || oyuncu.tickCount % 10 != 0) {
            return;
        }
        boolean eldeMi = oyuncu.getMainHandItem() == stack || oyuncu.getOffhandItem() == stack;

        if (!olumYeri.dimension().equals(level.dimension())) {
            if (eldeMi) {
                oyuncu.sendOverlayMessage(Component.translatable("mesaj.maceraaletleri.olum_baska_boyut"));
            }
            return;
        }

        double mesafe = Math.sqrt(olumYeri.pos().distToCenterSqr(oyuncu.position()));
        if (mesafe <= VARIS_MESAFESI) {
            stack.shrink(1);
            oyuncu.sendOverlayMessage(Component.translatable("mesaj.maceraaletleri.olum_varildi"));
            level.playSound(null, oyuncu.getX(), oyuncu.getY(), oyuncu.getZ(),
                    SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0F, 1.0F);
        } else if (eldeMi) {
            oyuncu.sendOverlayMessage(Component.translatable("mesaj.maceraaletleri.olum_mesafe", (int) mesafe));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        GlobalPos olumYeri = stack.get(ModDataComponents.KAYITLI_KONUM.get());
        if (olumYeri != null) {
            BlockPos p = olumYeri.pos();
            builder.accept(Component.translatable("tooltip.maceraaletleri.olum_yeri", p.getX(), p.getY(), p.getZ())
                    .withStyle(ChatFormatting.RED));
        }
    }
}
