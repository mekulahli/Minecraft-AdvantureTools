package com.ismail.maceraaletleri.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.ismail.maceraaletleri.Ayarlar;
import com.ismail.maceraaletleri.Basarimlar;
import com.ismail.maceraaletleri.Buyuler;
import com.ismail.maceraaletleri.MaceraAletleri;
import com.ismail.maceraaletleri.compat.Aksesuarlar;
import com.ismail.maceraaletleri.item.MiknatisYuzuguItem;
import com.ismail.maceraaletleri.item.PlanorItem;
import com.ismail.maceraaletleri.item.TirmanmaEldiveniItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

// Elde ya da aksesuar slotunda (Curios) takılıyken çalışan aletler: planör, tırmanma eldiveni, mıknatıs yüzüğü.
// Oyuncu hareketi istemcide hesaplandığı için hız değişiklikleri oyuncunun kendi bilgisayarında,
// düşme hasarı sıfırlama ve dayanıklılık gibi işler sunucuda yapılır.
@EventBusSubscriber(modid = MaceraAletleri.MODID)
public final class HareketOlaylari {
    private static final double TIRMANMA_HIZI = 0.2;
    private static final double KAYMA_HIZI = 0.1;
    private static final int PLANOR_ASINMA_ARALIGI = 40; // tick
    private static final int KUS_GIBI_SURESI = 10 * 20; // tick
    private static final int SICAK_HAVA_MENZILI = 24; // blok
    private static final double SICAK_HAVA_ITKISI = 0.12;
    private static final double SICAK_HAVA_MAKS = 0.6;

    // Sunucuda: her oyuncunun kesintisiz süzüldüğü süre.
    private static final Map<UUID, Integer> SUZULME_SURELERI = new HashMap<>();

    private HareketOlaylari() {
    }

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player oyuncu = event.getEntity();
        if (oyuncu.isSpectator()) {
            return;
        }
        yuzukSlotundakiMiknatis(oyuncu);
        if (oyuncu.getAbilities().flying || oyuncu.isPassenger()) {
            return;
        }
        planor(oyuncu);
        tirmanmaEldiveni(oyuncu);
    }

    // Envanterdeki yüzük kendi inventoryTick'iyle çalışır; aksesuar slotundaki yüzük için buradan çağırıyoruz.
    private static void yuzukSlotundakiMiknatis(Player oyuncu) {
        if (oyuncu.level() instanceof ServerLevel level) {
            ItemStack yuzuk = Aksesuarlar.bul(oyuncu, MiknatisYuzuguItem.class);
            if (MiknatisYuzuguItem.isAcik(yuzuk)) {
                MiknatisYuzuguItem.esyalariCek(level, oyuncu, yuzuk);
            }
        }
    }

    // --- Planör ---

    // Planörün açık olup olmadığı. Renderer de bunu kullanır (başın üstünde planör çizmek için).
    public static boolean planorAcik(Player oyuncu) {
        return !planorStack(oyuncu).isEmpty() && !oyuncu.onGround() && !oyuncu.isInWater() && !oyuncu.isInLava()
                && !oyuncu.isFallFlying() && !oyuncu.isShiftKeyDown() && !oyuncu.getAbilities().flying
                && !oyuncu.isPassenger() && !oyuncu.isSpectator();
    }

    private static ItemStack planorStack(Player oyuncu) {
        InteractionHand el = eldekiEl(oyuncu, PlanorItem.class);
        return el != null ? oyuncu.getItemInHand(el) : Aksesuarlar.bul(oyuncu, PlanorItem.class);
    }

    private static void planor(Player oyuncu) {
        boolean suzuluyor = planorAcik(oyuncu);
        boolean sicakHava = suzuluyor && sicakHavaVar(oyuncu);

        if (oyuncu instanceof ServerPlayer sunucuOyuncusu) {
            if (!suzuluyor) {
                SUZULME_SURELERI.remove(oyuncu.getUUID());
                return;
            }
            oyuncu.resetFallDistance();
            int sure = SUZULME_SURELERI.merge(oyuncu.getUUID(), 1, Integer::sum);
            if (sure % PLANOR_ASINMA_ARALIGI == 0) {
                planoruAsindir(oyuncu, eldekiEl(oyuncu, PlanorItem.class));
            }
            if (sure == KUS_GIBI_SURESI) {
                Basarimlar.ver(sunucuOyuncusu, Basarimlar.KUS_GIBI);
            }
            if (sicakHava) {
                Basarimlar.ver(sunucuOyuncusu, Basarimlar.SICAK_HAVA);
            }
            return;
        }

        Vec3 hiz = oyuncu.getDeltaMovement();
        // Sıcak hava yoksa sadece düşerken açılır; zıplarken ya da kancayla yukarı çekilirken karışmaz.
        if (!suzuluyor || !oyuncu.isLocalPlayer() || (!sicakHava && hiz.y >= 0)) {
            return;
        }

        // Süzülme büyüsü: seviye başına %25 daha yavaş düşüş, %15 daha hızlı süzülme.
        int suzulme = Buyuler.seviye(oyuncu.level(), Buyuler.SUZULME, planorStack(oyuncu));
        double dusmeHizi = Ayarlar.PLANOR_DUSME_HIZI.get() * (1 - 0.25 * suzulme);
        double ileriHiz = Ayarlar.PLANOR_ILERI_HIZ.get() * (1 + 0.15 * suzulme);

        Vec3 bakis = oyuncu.getLookAngle();
        Vec3 hedefYatay = new Vec3(bakis.x, 0, bakis.z).normalize().scale(ileriHiz);
        Vec3 yatay = new Vec3(hiz.x, 0, hiz.z).lerp(hedefYatay, 0.1);
        double dikey = sicakHava ? Math.min(hiz.y + SICAK_HAVA_ITKISI, SICAK_HAVA_MAKS) : Math.max(hiz.y, -dusmeHizi);
        oyuncu.setDeltaMovement(yatay.x, dikey, yatay.z);

        if (oyuncu.tickCount % (sicakHava ? 1 : 4) == 0) {
            oyuncu.level().addParticle(ParticleTypes.CLOUD, oyuncu.getX(), oyuncu.getY() + 2.2, oyuncu.getZ(), 0, -0.05, 0);
        }
    }

    // Breath of the Wild'daki gibi: altında (arada engel olmadan) yanan bir kamp ateşi ya da ateş varsa
    // sıcak hava planörü yukarı kaldırır.
    private static boolean sicakHavaVar(Player oyuncu) {
        BlockPos pos = oyuncu.blockPosition();
        for (int i = 1; i <= SICAK_HAVA_MENZILI; i++) {
            BlockState blok = oyuncu.level().getBlockState(pos.below(i));
            if ((blok.getBlock() instanceof CampfireBlock && blok.getValue(CampfireBlock.LIT)) || blok.is(BlockTags.FIRE)) {
                return true;
            }
            if (!blok.isAir()) {
                return false;
            }
        }
        return false;
    }

    private static void planoruAsindir(Player oyuncu, @Nullable InteractionHand el) {
        if (el != null) {
            oyuncu.getItemInHand(el).hurtAndBreak(1, oyuncu, el);
        } else if (!oyuncu.hasInfiniteMaterials()) {
            Aksesuarlar.guncelle(oyuncu, PlanorItem.class, planor -> {
                int hasar = planor.getDamageValue() + 1;
                if (hasar >= planor.getMaxDamage()) {
                    return ItemStack.EMPTY; // kırıldı
                }
                planor.setDamageValue(hasar);
                return planor;
            });
        }
    }

    // --- Tırmanma eldiveni ---
    // Duvara doğru yürü: tırman. Shift: duvarda asılı kal. Hiçbir tuşa basma: yavaşça kay.

    private static void tirmanmaEldiveni(Player oyuncu) {
        boolean takili = eldekiEl(oyuncu, TirmanmaEldiveniItem.class) != null
                || !Aksesuarlar.bul(oyuncu, TirmanmaEldiveniItem.class).isEmpty();
        if (!takili || !oyuncu.horizontalCollision) {
            return;
        }

        if (!oyuncu.level().isClientSide()) {
            oyuncu.resetFallDistance();
            return;
        }
        if (!oyuncu.isLocalPlayer()) {
            return;
        }

        Vec3 hiz = oyuncu.getDeltaMovement();
        double dikey;
        if (oyuncu.zza > 0) {
            dikey = TIRMANMA_HIZI;
        } else if (oyuncu.isShiftKeyDown()) {
            dikey = 0;
        } else {
            dikey = Math.max(hiz.y, -KAYMA_HIZI);
        }
        oyuncu.setDeltaMovement(hiz.x, dikey, hiz.z);
    }

    private static @Nullable InteractionHand eldekiEl(Player oyuncu, Class<?> esyaSinifi) {
        for (InteractionHand el : InteractionHand.values()) {
            if (esyaSinifi.isInstance(oyuncu.getItemInHand(el).getItem())) {
                return el;
            }
        }
        return null;
    }
}
