package com.ismail.maceraaletleri.item;

import org.jspecify.annotations.Nullable;

import com.ismail.maceraaletleri.Ayarlar;
import com.ismail.maceraaletleri.Buyuler;
import com.ismail.maceraaletleri.ModDataComponents;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// Envanterde (herhangi bir slotta) dururken, açıksa yakındaki eşyaları ve deneyim kürelerini çeker.
// Açıp kapatmak için: elde sağ tık ya da M tuşu (tuş ayarlardan değiştirilebilir).
public class MiknatisYuzuguItem extends Item {
    private static final double CEKME_HIZI = 0.35;

    public MiknatisYuzuguItem(Item.Properties properties) {
        super(properties);
    }

    public static boolean isAcik(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.MIKNATIS_ACIK.get(), false);
    }

    // Sunucuda çağrılır: yüzüğü aç/kapat, oyuncuya bildir.
    public static void degistir(Player player, ItemStack stack) {
        boolean acik = !isAcik(stack);
        stack.set(ModDataComponents.MIKNATIS_ACIK.get(), acik);
        player.sendOverlayMessage(Component.translatable(
                acik ? "mesaj.maceraaletleri.miknatis_acik" : "mesaj.maceraaletleri.miknatis_kapali"));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, acik ? 1.5F : 0.7F);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            degistir(player, player.getItemInHand(hand));
        }
        return InteractionResult.SUCCESS;
    }

    // Açıkken büyülü gibi parlasın.
    @Override
    public boolean isFoil(ItemStack stack) {
        return isAcik(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (isAcik(stack) && owner instanceof Player player) {
            esyalariCek(level, player, stack);
        }
    }

    // Envanterdeki yüzük (inventoryTick) ve aksesuar slotundaki yüzük (HareketOlaylari) bunu çağırır.
    // Çekim Gücü büyüsü: seviye başına +3 blok yarıçap.
    public static void esyalariCek(ServerLevel level, Player player, ItemStack yuzuk) {
        if (player.isSpectator() || player.isShiftKeyDown()) {
            return;
        }

        Vec3 merkez = player.position().add(0, player.getBbHeight() / 2, 0);
        int yaricap = Ayarlar.MIKNATIS_YARICAP.get() + 3 * Buyuler.seviye(level, Buyuler.CEKIM_GUCU, yuzuk);
        AABB alan = player.getBoundingBox().inflate(yaricap);

        for (ItemEntity esya : level.getEntitiesOfClass(ItemEntity.class, alan, e -> !e.hasPickUpDelay())) {
            cek(esya, merkez);
        }
        for (ExperienceOrb kure : level.getEntitiesOfClass(ExperienceOrb.class, alan)) {
            cek(kure, merkez);
        }
    }

    private static void cek(Entity entity, Vec3 hedef) {
        Vec3 yon = hedef.subtract(entity.position());
        if (yon.lengthSqr() < 1.0) {
            return;
        }
        entity.setDeltaMovement(yon.normalize().scale(CEKME_HIZI));
        entity.hurtMarked = true;
    }
}
