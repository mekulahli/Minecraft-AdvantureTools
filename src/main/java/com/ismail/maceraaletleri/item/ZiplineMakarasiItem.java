package com.ismail.maceraaletleri.item;

import java.util.function.Consumer;

import com.ismail.maceraaletleri.Ayarlar;
import com.ismail.maceraaletleri.ModDataComponents;
import com.ismail.maceraaletleri.entity.ZiplineEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

// Bir bloğa sağ tık: başlangıç noktası. Başka bir bloğa sağ tık: iki nokta arasına zipline hattı gerer.
// Aynı bloğa tekrar sağ tık seçimi iptal eder.
public class ZiplineMakarasiItem extends Item {
    private static final double MIN_UZUNLUK = 3.0;

    public ZiplineMakarasiItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level) || context.getPlayer() == null) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        BlockPos tiklanan = context.getClickedPos();
        GlobalPos baslangic = stack.get(ModDataComponents.ZIPLINE_BASLANGIC.get());

        if (baslangic == null || !baslangic.dimension().equals(level.dimension())) {
            stack.set(ModDataComponents.ZIPLINE_BASLANGIC.get(), GlobalPos.of(level.dimension(), tiklanan));
            mesaj(player, "mesaj.maceraaletleri.zipline_baslangic");
            level.playSound(null, tiklanan, SoundEvents.LEAD_TIED, SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        if (baslangic.pos().equals(tiklanan)) {
            stack.remove(ModDataComponents.ZIPLINE_BASLANGIC.get());
            mesaj(player, "mesaj.maceraaletleri.zipline_iptal");
            return InteractionResult.SUCCESS;
        }

        double uzunluk = Math.sqrt(baslangic.pos().distSqr(tiklanan));
        if (uzunluk > Ayarlar.ZIPLINE_UZUNLUK.get()) {
            player.sendOverlayMessage(Component.translatable("mesaj.maceraaletleri.zipline_uzun",
                    Ayarlar.ZIPLINE_UZUNLUK.get()));
            return InteractionResult.FAIL;
        }
        if (uzunluk < MIN_UZUNLUK) {
            mesaj(player, "mesaj.maceraaletleri.zipline_kisa");
            return InteractionResult.FAIL;
        }

        ZiplineEntity.hatKur(level, baslangic.pos(), tiklanan);
        stack.remove(ModDataComponents.ZIPLINE_BASLANGIC.get());
        stack.consume(1, player);
        mesaj(player, "mesaj.maceraaletleri.zipline_kuruldu");
        level.playSound(null, tiklanan, SoundEvents.LEAD_TIED, SoundSource.PLAYERS, 1.0F, 0.7F);
        return InteractionResult.SUCCESS;
    }

    private static void mesaj(Player player, String anahtar) {
        player.sendOverlayMessage(Component.translatable(anahtar));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        GlobalPos baslangic = stack.get(ModDataComponents.ZIPLINE_BASLANGIC.get());
        if (baslangic != null) {
            BlockPos p = baslangic.pos();
            builder.accept(Component.translatable("tooltip.maceraaletleri.zipline_baslangic",
                    p.getX(), p.getY(), p.getZ()).withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(ModDataComponents.ZIPLINE_BASLANGIC.get());
    }
}
