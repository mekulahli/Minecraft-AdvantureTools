package com.ismail.maceraaletleri.entity;

import org.jspecify.annotations.Nullable;

import com.ismail.maceraaletleri.ModEntities;
import com.ismail.maceraaletleri.ModItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

// Atılan işaret fişeği. Bir yere düşünce 60 saniye boyunca yanar: etrafı aydınlatır (görünmez vanilla
// ışık bloğu koyarak) ve uzaktan görünen kırmızı duman çıkarır. Sönünce ışık bloğunu kaldırır.
public class IsaretFisegiEntity extends ThrowableItemProjectile {
    private static final int YANMA_SURESI = 60 * 20;
    private static final int DUMAN_RENGI = 0xFF3030;

    private static final EntityDataAccessor<Boolean> YANIYOR =
            SynchedEntityData.defineId(IsaretFisegiEntity.class, EntityDataSerializers.BOOLEAN);

    private @Nullable BlockPos isikPos;
    private int yanmaSayaci;

    public IsaretFisegiEntity(EntityType<? extends IsaretFisegiEntity> type, Level level) {
        super(type, level);
    }

    public IsaretFisegiEntity(Level level, LivingEntity atan, ItemStack stack) {
        super(ModEntities.ISARET_FISEGI.get(), atan, level, stack);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(YANIYOR, false);
    }

    public boolean isYaniyor() {
        return this.getEntityData().get(YANIYOR);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ISARET_FISEGI.get();
    }

    @Override
    protected double getDefaultGravity() {
        return isYaniyor() ? 0.0 : 0.03;
    }

    // Canlılara çarpmaz, içlerinden geçip yere düşer.
    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (isYaniyor()) {
            return;
        }
        super.onHitBlock(hit);
        // Bloğun biraz dışında dursun ki ışık bloğu havaya konabilsin.
        Vec3 konum = hit.getLocation().add(Vec3.atLowerCornerOf(hit.getDirection().getUnitVec3i()).scale(0.1));
        this.setPos(konum);
        this.setDeltaMovement(Vec3.ZERO);
        if (!this.level().isClientSide()) {
            this.getEntityData().set(YANIYOR, true);
            isikKoy();
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 0.8F, 1.4F);
        }
    }

    private void isikKoy() {
        BlockPos pos = this.blockPosition();
        if (this.level().getBlockState(pos).isAir()) {
            this.level().setBlock(pos, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 15), 3);
            isikPos = pos;
        }
    }

    @Override
    public void tick() {
        if (isYaniyor()) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        super.tick();

        if (this.level().isClientSide()) {
            parcaciklar();
        } else if (isYaniyor() && ++yanmaSayaci >= YANMA_SURESI) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.NEUTRAL, 0.5F, 1.2F);
            this.discard();
        }
    }

    private void parcaciklar() {
        Level level = this.level();
        if (!isYaniyor()) {
            level.addParticle(ParticleTypes.SMALL_FLAME, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            return;
        }
        level.addParticle(ParticleTypes.FLAME, this.getX(), this.getY() + 0.1, this.getZ(),
                (random.nextDouble() - 0.5) * 0.05, 0.05, (random.nextDouble() - 0.5) * 0.05);
        level.addParticle(new DustParticleOptions(DUMAN_RENGI, 2.0F), this.getX(), this.getY() + 0.3, this.getZ(), 0, 0.1, 0);
        if (this.tickCount % 3 == 0) {
            // Kamp ateşi sinyal dumanı çok yükseğe çıkar ve uzaktan görünür.
            level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true,
                    this.getX(), this.getY() + 0.5, this.getZ(), 0, 0.07, 0);
        }
    }

    // Kalıcı olarak kaldırılırken ışığı da kaldır. (Chunk boşaltılırken değil; o zaman fişek kaydedilir.)
    @Override
    public void remove(Entity.RemovalReason reason) {
        if (reason.shouldDestroy() && !this.level().isClientSide() && isikPos != null
                && this.level().getBlockState(isikPos).is(Blocks.LIGHT)) {
            this.level().removeBlock(isikPos, false);
        }
        super.remove(reason);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Yaniyor", isYaniyor());
        output.putInt("YanmaSayaci", yanmaSayaci);
        output.storeNullable("IsikPos", BlockPos.CODEC, isikPos);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.getEntityData().set(YANIYOR, input.getBooleanOr("Yaniyor", false));
        yanmaSayaci = input.getIntOr("YanmaSayaci", 0);
        isikPos = input.read("IsikPos", BlockPos.CODEC).orElse(null);
    }
}
