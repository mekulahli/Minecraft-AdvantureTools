package com.ismail.maceraaletleri.entity;

import org.jspecify.annotations.Nullable;

import com.ismail.maceraaletleri.Ayarlar;
import com.ismail.maceraaletleri.Basarimlar;
import com.ismail.maceraaletleri.Buyuler;
import com.ismail.maceraaletleri.ModEntities;
import com.ismail.maceraaletleri.ModItems;
import com.ismail.maceraaletleri.item.KancaItem;
import com.ismail.maceraaletleri.item.KancaSeviyesi;
import com.ismail.maceraaletleri.item.ManevraTakimiItem;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

// Fırlatılan kanca. İki şekilde takılabilir:
//  - Bloğa takılırsa sahibini (oyuncuyu) kendine çeker. Shift basılıyken çekmez, ip sabit kalır ve oyuncu sallanır.
//  - Moba ya da yerdeki eşyaya takılırsa onu sahibine doğru çeker.
// Zipline'lar da bu sınıfı kullanır: hattın öbür ucuna önceden takılı bir kanca oluşturulur.
public class KancaEntity extends ThrowableItemProjectile {
    private static final double VARIS_MESAFESI = 1.5;
    private static final double MOB_BIRAKMA_MESAFESI = 2.0;
    private static final double MIN_IP_UZUNLUGU = 1.5;
    private static final double IP_GERGINLIGI = 0.2;
    private static final double TIRMANMA_HIZI = 0.15;
    private static final int TARZAN_SURESI = 15 * 20; // tick
    private static final int HEDEF_YOK = -1;

    // Sunucu bu değerleri değiştirince istemciye otomatik gönderilir; ip çizimi ve çekme bunları kullanır.
    private static final EntityDataAccessor<Boolean> TAKILI =
            SynchedEntityData.defineId(KancaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> HEDEF_ID =
            SynchedEntityData.defineId(KancaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ZIPLINE =
            SynchedEntityData.defineId(KancaEntity.class, EntityDataSerializers.BOOLEAN);
    // 3D manevra takımı kancası mı; öyleyse hangi taraf (ip o kalçadan çıkar).
    private static final EntityDataAccessor<Byte> TARAF =
            SynchedEntityData.defineId(KancaEntity.class, EntityDataSerializers.BYTE);

    public static final byte TARAF_YOK = 0;
    public static final byte TARAF_SOL = 1;
    public static final byte TARAF_SAG = 2;
    private static final double MANEVRA_MENZIL = 40.0;
    private static final double MANEVRA_VARIS = 2.0;
    private static final double MANEVRA_MAKS_HIZ = 1.6;
    private static final double MANEVRA_ITKI = 0.05;

    // Sallanma modundaki ip uzunluğu. Sadece oyuncunun kendi bilgisayarında kullanılır; -1 = sallanmıyor.
    private double ipUzunlugu = -1;
    // Sunucuda: kesintisiz sallanılan süre (Tarzan başarımı için).
    private int sallanmaSuresi;

    public KancaEntity(EntityType<? extends KancaEntity> type, Level level) {
        super(type, level);
    }

    public KancaEntity(Level level, Player sahip, ItemStack stack) {
        super(ModEntities.KANCA.get(), sahip, level, stack);
    }

    // Zipline için: hattın öbür ucuna zaten takılı olarak doğan kanca.
    public static KancaEntity zipline(Level level, Player sahip, Vec3 bitis) {
        KancaEntity kanca = new KancaEntity(level, sahip, new ItemStack(ModItems.ELMAS_KANCA.get()));
        kanca.setPos(bitis);
        kanca.getEntityData().set(TAKILI, true);
        kanca.getEntityData().set(ZIPLINE, true);
        return kanca;
    }

    // 3D manevra takımının sol ya da sağ kancası.
    public static KancaEntity manevra(Level level, Player sahip, byte taraf) {
        KancaEntity kanca = new KancaEntity(level, sahip, new ItemStack(ModItems.DEMIR_KANCA.get()));
        kanca.getEntityData().set(TARAF, taraf);
        return kanca;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TAKILI, false);
        builder.define(HEDEF_ID, HEDEF_YOK);
        builder.define(ZIPLINE, false);
        builder.define(TARAF, TARAF_YOK);
    }

    public byte getTaraf() {
        return this.getEntityData().get(TARAF);
    }

    public boolean isManevra() {
        return getTaraf() != TARAF_YOK;
    }

    public boolean isTakili() {
        return this.getEntityData().get(TAKILI);
    }

    public boolean isZipline() {
        return this.getEntityData().get(ZIPLINE);
    }

    public @Nullable Entity getHedef() {
        int id = this.getEntityData().get(HEDEF_ID);
        return id == HEDEF_YOK ? null : this.level().getEntity(id);
    }

    // Kancanın seviyesi, fırlatılan eşyadan okunur. Eşya istemciye de gönderildiği için iki tarafta da çalışır.
    public KancaSeviyesi getSeviye() {
        return this.getItem().getItem() instanceof KancaItem kanca ? kanca.getSeviye() : KancaSeviyesi.DEMIR;
    }

    public static double menzil(KancaSeviyesi seviye) {
        return seviye.menzil() * Ayarlar.KANCA_MENZIL_CARPANI.get();
    }

    // Menzil büyüsü: seviye başına %20 daha uzun ip.
    private double menzil() {
        if (isZipline()) {
            return Ayarlar.ZIPLINE_UZUNLUK.get() + 8;
        }
        if (isManevra()) {
            return MANEVRA_MENZIL;
        }
        return menzil(getSeviye()) * (1 + 0.2 * Buyuler.seviye(this.level(), Buyuler.MENZIL, this.getItem()));
    }

    // Hızlı Çekim büyüsü: seviye başına %15 daha hızlı.
    private double cekmeHizi() {
        return getSeviye().cekmeHizi() * Ayarlar.KANCA_HIZ_CARPANI.get()
                * (1 + 0.15 * Buyuler.seviye(this.level(), Buyuler.HIZLI_CEKIM, this.getItem()));
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.DEMIR_KANCA.get();
    }

    // Takılıyken yerinde sabit dursun, düşmesin.
    @Override
    protected double getDefaultGravity() {
        return isBagli() ? 0.0 : 0.03;
    }

    private boolean isBagli() {
        return isTakili() || this.getEntityData().get(HEDEF_ID) != HEDEF_YOK;
    }

    // Kanca canlılara ve yerdeki eşyalara takılabilir (ayarlardan kapatılabilir); sahibine takılmaz.
    @Override
    protected boolean canHitEntity(Entity target) {
        if (target == this.getOwner() || isBagli() || isManevra() || !Ayarlar.KANCA_MOB_CEKME.get()) {
            return false;
        }
        if (target instanceof ItemEntity) {
            return target.isAlive();
        }
        return target instanceof LivingEntity && super.canHitEntity(target);
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (isBagli()) {
            return;
        }
        super.onHitBlock(hit);
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(hit.getLocation());
        if (this.level() instanceof ServerLevel serverLevel) {
            this.getEntityData().set(TAKILI, true);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.0F, 1.0F);
            serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(),
                    10, 0.1, 0.1, 0.1, 0.25);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (isBagli()) {
            return;
        }
        super.onHitEntity(hit);
        this.setDeltaMovement(Vec3.ZERO);
        if (this.level() instanceof ServerLevel serverLevel) {
            this.getEntityData().set(HEDEF_ID, hit.getEntity().getId());
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.CHAIN_HIT, SoundSource.PLAYERS, 1.0F, 1.2F);
            serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(),
                    10, 0.2, 0.2, 0.2, 0.25);
        }
    }

    @Override
    public void tick() {
        if (isBagli()) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        super.tick();
        if (this.isRemoved()) {
            return;
        }

        if (!(this.getOwner() instanceof Player sahip) || !sahip.isAlive() || sahip.level() != this.level()) {
            birak();
            return;
        }

        if (this.getEntityData().get(HEDEF_ID) != HEDEF_YOK) {
            Entity hedef = getHedef();
            if (hedef == null || !hedef.isAlive()) {
                birak();
                return;
            }
            // Kanca hedefin üstünde dursun ki ip hedefe bağlı görünsün.
            this.setPos(hedef.getX(), hedef.getY(0.5), hedef.getZ());
            hedefiCek(sahip, hedef);
            return;
        }

        Vec3 sahiptenKancaya = this.position().subtract(govdeMerkezi(sahip));
        double mesafe = sahiptenKancaya.length();

        if (mesafe > menzil()) {
            birak();
            return;
        }

        if (isTakili()) {
            sahibiCek(sahip, sahiptenKancaya, mesafe);
        }
    }

    private void sahibiCek(Player sahip, Vec3 sahiptenKancaya, double mesafe) {
        if (isManevra()) {
            manevraCek(sahip, sahiptenKancaya, mesafe);
            return;
        }
        // Zipline'da Shift ile sallanılmaz, hat boyunca kayılır.
        boolean sallaniyor = sahip.isShiftKeyDown() && !isZipline();

        if (sahip instanceof ServerPlayer sunucuOyuncusu) {
            sunucuTarafi(sunucuOyuncusu, sallaniyor, mesafe);
            return;
        }

        // Oyuncu hareketini istemci hesaplar, bu yüzden çekme ve sallanma oyuncunun kendi bilgisayarında uygulanır.
        if (!sahip.isLocalPlayer()) {
            return;
        }

        if (sallaniyor) {
            sallan(sahip, sahiptenKancaya, mesafe);
        } else {
            ipUzunlugu = -1;
            Vec3 hedefHiz = sahiptenKancaya.normalize().scale(cekmeHizi());
            sahip.setDeltaMovement(sahip.getDeltaMovement().scale(0.5).add(hedefHiz.scale(0.5)));
        }
    }

    private void sunucuTarafi(ServerPlayer sahip, boolean sallaniyor, double mesafe) {
        // Çekilirken biriken düşme mesafesini sıfırla; yoksa varınca düşme hasarı alırsın.
        sahip.resetFallDistance();

        // Gerilen ipin gıcırtısı.
        if (this.tickCount % 20 == 0) {
            this.level().playSound(null, sahip.getX(), sahip.getY(), sahip.getZ(),
                    SoundEvents.LEAD_TIED, SoundSource.PLAYERS, 0.4F, 0.5F + this.random.nextFloat() * 0.2F);
        }

        if (sallaniyor && !sahip.onGround()) {
            if (++sallanmaSuresi == TARZAN_SURESI) {
                Basarimlar.ver(sahip, Basarimlar.TARZAN);
            }
        } else {
            sallanmaSuresi = 0;
        }

        if (!sallaniyor && mesafe < VARIS_MESAFESI) {
            if (isZipline()) {
                Basarimlar.ver(sahip, Basarimlar.ZIPLINE);
            }
            birak();
        }
    }

    // 3D manevra: hızı tamamen değiştirmek yerine kancaya doğru ivme verir, böylece iki kanca birlikte
    // çekince kuvvetler toplanır ve oyuncu aralarından savrularak geçer. Zıplama tuşu bakılan yöne gaz püskürtür.
    private void manevraCek(Player sahip, Vec3 sahiptenKancaya, double mesafe) {
        ItemStack takim = sahip.getMainHandItem();
        if (!(takim.getItem() instanceof ManevraTakimiItem)) {
            birak();
            return;
        }

        if (sahip instanceof ServerPlayer sunucuOyuncusu) {
            sunucuOyuncusu.resetFallDistance();
            boolean itki = sunucuOyuncusu.getLastClientInput().jump();
            int maliyet = Ayarlar.MANEVRA_GAZ_TUKETIMI.get() + (itki ? 1 : 0);
            if (!ManevraTakimiItem.gazHarca(sunucuOyuncusu, takim, maliyet)) {
                sunucuOyuncusu.sendOverlayMessage(Component.translatable("mesaj.maceraaletleri.gaz_bitti"));
                birak();
                return;
            }
            if (this.tickCount % 10 == 0) {
                this.level().playSound(null, sahip.getX(), sahip.getY(), sahip.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.15F, 1.8F);
            }
            if (ikiKancaTakili(sunucuOyuncusu)) {
                Basarimlar.ver(sunucuOyuncusu, Basarimlar.TITAN_AVCISI);
            }
            if (mesafe < MANEVRA_VARIS) {
                birak();
            }
            return;
        }

        if (!sahip.isLocalPlayer() || (ManevraTakimiItem.gaz(takim) <= 0 && !sahip.hasInfiniteMaterials())) {
            return;
        }
        Vec3 hiz = sahip.getDeltaMovement().add(sahiptenKancaya.normalize().scale(Ayarlar.MANEVRA_IVME.get()));
        if (sahip.isJumping()) {
            hiz = hiz.add(sahip.getLookAngle().scale(MANEVRA_ITKI));
            this.level().addParticle(ParticleTypes.CLOUD, sahip.getX(), sahip.getY() + 0.8, sahip.getZ(), 0, 0, 0);
        }
        if (hiz.length() > MANEVRA_MAKS_HIZ) {
            hiz = hiz.normalize().scale(MANEVRA_MAKS_HIZ);
        }
        sahip.setDeltaMovement(hiz);
    }

    private boolean ikiKancaTakili(ServerPlayer sahip) {
        return this.level().getEntitiesOfClass(KancaEntity.class, sahip.getBoundingBox().inflate(MANEVRA_MENZIL + 8),
                k -> k.getOwner() == sahip && k.isManevra() && k.isTakili()).size() >= 2;
    }

    // Sarkaç: oyuncu ipin boyundan uzağa gidemez. Yerçekimi ve oyuncunun kendi hareketi salınımı oluşturur.
    // Zıplama tuşu ipte yukarı tırmandırır, geri tuşu (S) ipi uzatıp aşağı indirir.
    private void sallan(Player sahip, Vec3 sahiptenKancaya, double mesafe) {
        if (ipUzunlugu < 0) {
            ipUzunlugu = mesafe;
        }
        if (sahip.isJumping()) {
            ipUzunlugu = Math.max(MIN_IP_UZUNLUGU, ipUzunlugu - TIRMANMA_HIZI);
        } else if (sahip.zza < 0) {
            ipUzunlugu = Math.min(menzil() - 1, ipUzunlugu + TIRMANMA_HIZI);
        }
        if (mesafe <= ipUzunlugu) {
            return;
        }

        Vec3 disari = sahiptenKancaya.normalize().reverse();
        Vec3 hiz = sahip.getDeltaMovement();
        double disariHiz = hiz.dot(disari);
        if (disariHiz > 0) {
            hiz = hiz.subtract(disari.scale(disariHiz));
        }
        // İp esnemesin: fazla uzadıysa oyuncuyu hafifçe geri çek.
        hiz = hiz.subtract(disari.scale((mesafe - ipUzunlugu) * IP_GERGINLIGI));
        sahip.setDeltaMovement(hiz);
    }

    // Mob ve eşya hareketi sunucuda hesaplanır, o yüzden çekme sunucuda yapılır.
    private void hedefiCek(Player sahip, Entity hedef) {
        if (!(sahip instanceof ServerPlayer sunucuOyuncusu)) {
            return;
        }

        Vec3 hedeftenSahibe = govdeMerkezi(sahip).subtract(hedef.position());
        double mesafe = hedeftenSahibe.length();
        if (mesafe < MOB_BIRAKMA_MESAFESI) {
            if (hedef instanceof LivingEntity) {
                Basarimlar.ver(sunucuOyuncusu, Basarimlar.GEL_BURAYA);
            }
            birak();
            return;
        }
        if (mesafe > menzil()) {
            birak();
            return;
        }

        Vec3 hiz = hedeftenSahibe.normalize().scale(cekmeHizi() * 0.7);
        hedef.setDeltaMovement(hiz.add(0, 0.05, 0));
        hedef.hurtMarked = true; // yeni hızı hemen istemcilere gönder
        hedef.resetFallDistance();
    }

    private void birak() {
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }

    private static Vec3 govdeMerkezi(Entity entity) {
        return entity.position().add(0, entity.getBbHeight() / 2, 0);
    }
}
