package com.ismail.maceraaletleri.entity;

import com.ismail.maceraaletleri.ModEntities;
import com.ismail.maceraaletleri.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// Zipline hattının bir ucu. Her hat iki uçtan oluşur; iki uç da birbirini gösterir, ipi sadece "ana" uç çizer.
//  - Uca sağ tık: öbür uca doğru kayarsın (aslında öbür uçta takılı bir kanca oluşur ve seni çeker).
//  - Uca vur: hat kopar, makara geri düşer.
//  - Bağlı olduğu blok kırılırsa hat kopar.
public class ZiplineEntity extends Entity {
    private static final EntityDataAccessor<BlockPos> BITIS =
            SynchedEntityData.defineId(ZiplineEntity.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Boolean> ANA_UC =
            SynchedEntityData.defineId(ZiplineEntity.class, EntityDataSerializers.BOOLEAN);

    private BlockPos bagliBlok = BlockPos.ZERO;

    public ZiplineEntity(EntityType<? extends ZiplineEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    // İki bloğun arasına hat kurar: her bloğun üstüne bir uç koyar.
    public static void hatKur(ServerLevel level, BlockPos a, BlockPos b) {
        level.addFreshEntity(ucOlustur(level, a, b, true));
        level.addFreshEntity(ucOlustur(level, b, a, false));
    }

    private static ZiplineEntity ucOlustur(ServerLevel level, BlockPos blok, BlockPos karsiBlok, boolean ana) {
        ZiplineEntity uc = new ZiplineEntity(ModEntities.ZIPLINE.get(), level);
        uc.bagliBlok = blok;
        uc.setPos(baglantiNoktasi(blok));
        uc.getEntityData().set(BITIS, karsiBlok);
        uc.getEntityData().set(ANA_UC, ana);
        return uc;
    }

    // Hattın bloğa bağlandığı nokta: bloğun üst yüzeyinin ortası.
    public static Vec3 baglantiNoktasi(BlockPos blok) {
        return Vec3.atBottomCenterOf(blok).add(0, 1.0, 0);
    }

    public BlockPos getBitis() {
        return this.getEntityData().get(BITIS);
    }

    public boolean isAnaUc() {
        return this.getEntityData().get(ANA_UC);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BITIS, BlockPos.ZERO);
        builder.define(ANA_UC, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level && this.tickCount % 20 == 0
                && level.getBlockState(bagliBlok).isAir()) {
            kopar(level, true);
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        if (this.level() instanceof ServerLevel level) {
            // Oyuncunun havadaki kancalarını topla, sonra öbür uçta takılı bir kanca oluştur.
            level.getEntitiesOfClass(KancaEntity.class, player.getBoundingBox().inflate(128),
                    k -> k.getOwner() == player).forEach(Entity::discard);
            level.addFreshEntity(KancaEntity.zipline(level, player, baglantiNoktasi(getBitis())));
            level.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 1.0F, 0.5F);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() instanceof Player player) {
            kopar(level, !player.isCreative());
            return true;
        }
        return false;
    }

    // Bu ucu ve öbür ucu kaldırır; istenirse makarayı düşürür.
    private void kopar(ServerLevel level, boolean makaraDusur) {
        if (this.isRemoved()) {
            return;
        }
        Vec3 karsiNokta = baglantiNoktasi(getBitis());
        level.getEntitiesOfClass(ZiplineEntity.class, new AABB(karsiNokta, karsiNokta).inflate(0.5),
                uc -> uc.getBitis().equals(this.bagliBlok)).forEach(Entity::discard);
        this.discard();

        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LEAD_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (makaraDusur) {
            this.spawnAtLocation(level, new ItemStack(ModItems.ZIPLINE_MAKARASI.get()));
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("Bitis", BlockPos.CODEC, getBitis());
        output.store("BagliBlok", BlockPos.CODEC, bagliBlok);
        output.putBoolean("AnaUc", isAnaUc());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.getEntityData().set(BITIS, input.read("Bitis", BlockPos.CODEC).orElse(BlockPos.ZERO));
        this.bagliBlok = input.read("BagliBlok", BlockPos.CODEC).orElse(this.blockPosition().below());
        this.getEntityData().set(ANA_UC, input.getBooleanOr("AnaUc", false));
    }
}
