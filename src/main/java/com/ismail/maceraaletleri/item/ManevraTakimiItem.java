package com.ismail.maceraaletleri.item;

import java.util.function.Consumer;

import com.ismail.maceraaletleri.ModDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

// 3D Manevra Takımı: sol tık sol kancayı, sağ tık sağ kancayı atar (tekrar basınca geri çeker).
// Takılı kancalar gaz harcayarak oyuncuyu kendilerine doğru hızlandırır; iki kanca birlikte kullanılabilir.
// Tıklamalar istemcide yakalanıp paketle sunucuya gönderilir (client/MaceraAletleriClient, network/Paketler).
// Gaz bitince envanterdeki gaz tüpü otomatik takılır.
public class ManevraTakimiItem extends Item {
    public static final int GAZ_MAKS = 1000;
    public static final int ATES_MALIYETI = 15;
    private static final int GAZ_RENGI = 0x7FD4FF;

    public ManevraTakimiItem(Item.Properties properties) {
        super(properties);
    }

    public static int gaz(ItemStack takim) {
        return takim.getOrDefault(ModDataComponents.GAZ.get(), 0);
    }

    // Sunucuda çağrılır. Gaz yetmezse envanterden bir tüp takmayı dener. Harcanabildiyse true.
    public static boolean gazHarca(Player oyuncu, ItemStack takim, int miktar) {
        if (oyuncu.hasInfiniteMaterials() || miktar <= 0) {
            return true;
        }
        if (gaz(takim) < miktar && !tupTak(oyuncu, takim)) {
            return false;
        }
        takim.set(ModDataComponents.GAZ.get(), gaz(takim) - miktar);
        return true;
    }

    private static boolean tupTak(Player oyuncu, ItemStack takim) {
        Inventory envanter = oyuncu.getInventory();
        for (int i = 0; i < envanter.getContainerSize(); i++) {
            ItemStack tup = envanter.getItem(i);
            if (tup.getItem() instanceof GazTupuItem) {
                tup.shrink(1);
                takim.set(ModDataComponents.GAZ.get(), GAZ_MAKS);
                oyuncu.sendOverlayMessage(Component.translatable("mesaj.maceraaletleri.gaz_takildi"));
                oyuncu.level().playSound(null, oyuncu.getX(), oyuncu.getY(), oyuncu.getZ(),
                        SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.PLAYERS, 1.0F, 1.3F);
                return true;
            }
        }
        return false;
    }

    // Kancalar tıklama olayıyla atılır; normal "kullan" davranışı yok.
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    // Dayanıklılık çubuğunun yerinde gaz göstergesi.
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Mth.clamp(Math.round(13.0F * gaz(stack) / GAZ_MAKS), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return GAZ_RENGI;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.maceraaletleri.gaz", gaz(stack), GAZ_MAKS)
                .withStyle(ChatFormatting.AQUA));
    }
}
