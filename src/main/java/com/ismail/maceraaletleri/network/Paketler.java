package com.ismail.maceraaletleri.network;

import java.util.List;

import com.ismail.maceraaletleri.Basarimlar;
import com.ismail.maceraaletleri.MaceraAletleri;
import com.ismail.maceraaletleri.compat.Aksesuarlar;
import com.ismail.maceraaletleri.entity.KancaEntity;
import com.ismail.maceraaletleri.item.ManevraTakimiItem;
import com.ismail.maceraaletleri.item.MiknatisYuzuguItem;
import com.ismail.maceraaletleri.item.SirtCantasiItem;
import com.ismail.maceraaletleri.item.ZiplamaBotuItem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

// İstemciden sunucuya giden tüm paketler ve sunucudaki karşılıkları.
// İstemci sadece "şunu yapmak istiyorum" der; her şey sunucuda tekrar kontrol edilir (hileye karşı).
@EventBusSubscriber(modid = MaceraAletleri.MODID)
public final class Paketler {
    private Paketler() {
    }

    @SubscribeEvent
    static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar kayit = event.registrar("2");
        kayit.playToServer(MiknatisDegistirPaketi.TYPE, MiknatisDegistirPaketi.STREAM_CODEC,
                (paket, context) -> miknatisDegistir((ServerPlayer) context.player()));
        kayit.playToServer(ManevraAtesPaketi.TYPE, ManevraAtesPaketi.STREAM_CODEC,
                (paket, context) -> manevraAtes((ServerPlayer) context.player(), paket.sol()));
        kayit.playToServer(CiftZiplamaPaketi.TYPE, CiftZiplamaPaketi.STREAM_CODEC,
                (paket, context) -> ciftZiplama((ServerPlayer) context.player()));
        kayit.playToServer(CantaAcPaketi.TYPE, CantaAcPaketi.STREAM_CODEC,
                (paket, context) -> cantaAc((ServerPlayer) context.player()));
    }

    // M tuşu: envanterdeki (yoksa yüzük slotundaki) mıknatıs yüzüğünü aç/kapat.
    private static void miknatisDegistir(ServerPlayer oyuncu) {
        Inventory envanter = oyuncu.getInventory();
        for (int i = 0; i < envanter.getContainerSize(); i++) {
            if (envanter.getItem(i).getItem() instanceof MiknatisYuzuguItem) {
                MiknatisYuzuguItem.degistir(oyuncu, envanter.getItem(i));
                return;
            }
        }
        Aksesuarlar.guncelle(oyuncu, MiknatisYuzuguItem.class, yuzuk -> {
            MiknatisYuzuguItem.degistir(oyuncu, yuzuk);
            return yuzuk;
        });
    }

    // Sol/sağ tık: o taraftaki kanca dışarıdaysa geri çek, değilse gaz harcayıp at.
    private static void manevraAtes(ServerPlayer oyuncu, boolean sol) {
        ItemStack takim = oyuncu.getMainHandItem();
        if (!(takim.getItem() instanceof ManevraTakimiItem) || !(oyuncu.level() instanceof ServerLevel level)) {
            return;
        }
        byte taraf = sol ? KancaEntity.TARAF_SOL : KancaEntity.TARAF_SAG;

        List<KancaEntity> mevcut = level.getEntitiesOfClass(KancaEntity.class, oyuncu.getBoundingBox().inflate(56),
                k -> k.getOwner() == oyuncu && k.getTaraf() == taraf);
        if (!mevcut.isEmpty()) {
            mevcut.forEach(Entity::discard);
            level.playSound(null, oyuncu.getX(), oyuncu.getY(), oyuncu.getZ(),
                    SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 0.8F, 1.5F);
            return;
        }

        if (!ManevraTakimiItem.gazHarca(oyuncu, takim, ManevraTakimiItem.ATES_MALIYETI)) {
            oyuncu.sendOverlayMessage(Component.translatable("mesaj.maceraaletleri.gaz_bitti"));
            return;
        }
        KancaEntity kanca = KancaEntity.manevra(level, oyuncu, taraf);
        // Kancalar hafifçe sola ve sağa açılarak çıkar.
        kanca.shootFromRotation(oyuncu, oyuncu.getXRot(), oyuncu.getYRot() + (sol ? -5.0F : 5.0F), 0.0F, 3.0F, 0.2F);
        level.addFreshEntity(kanca);
        level.playSound(null, oyuncu.getX(), oyuncu.getY(), oyuncu.getZ(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 0.8F, 1.4F);
    }

    private static void ciftZiplama(ServerPlayer oyuncu) {
        if (!(oyuncu.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ZiplamaBotuItem) || oyuncu.onGround()) {
            return;
        }
        oyuncu.resetFallDistance();
        oyuncu.getItemBySlot(EquipmentSlot.FEET).hurtAndBreak(1, oyuncu, EquipmentSlot.FEET);
        ServerLevel level = (ServerLevel) oyuncu.level();
        level.playSound(null, oyuncu.getX(), oyuncu.getY(), oyuncu.getZ(), SoundEvents.BREEZE_JUMP, SoundSource.PLAYERS, 0.7F, 1.2F);
        level.sendParticles(ParticleTypes.POOF, oyuncu.getX(), oyuncu.getY(), oyuncu.getZ(), 8, 0.3, 0.05, 0.3, 0.02);
        Basarimlar.ver(oyuncu, Basarimlar.CIFT_ZIPLAMA);
    }

    // B tuşu: göğüs slotunda (sırtta) takılı çantayı aç.
    private static void cantaAc(ServerPlayer oyuncu) {
        ItemStack canta = oyuncu.getItemBySlot(EquipmentSlot.CHEST);
        if (canta.getItem() instanceof SirtCantasiItem cantaItem) {
            cantaItem.ac(oyuncu, canta, EquipmentSlot.CHEST.getIndex(Inventory.INVENTORY_SIZE));
        } else {
            oyuncu.sendOverlayMessage(Component.translatable("mesaj.maceraaletleri.canta_yok"));
        }
    }
}
