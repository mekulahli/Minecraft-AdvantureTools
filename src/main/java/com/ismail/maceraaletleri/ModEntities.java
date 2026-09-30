package com.ismail.maceraaletleri;

import com.ismail.maceraaletleri.entity.IsaretFisegiEntity;
import com.ismail.maceraaletleri.entity.KancaEntity;
import com.ismail.maceraaletleri.entity.ZiplineEntity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// Modun entity türleri (fırlatılan kanca, ileride robot vb.)
public final class ModEntities {
    public static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(MaceraAletleri.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<KancaEntity>> KANCA =
            ENTITIES.registerEntityType("kanca", KancaEntity::new, MobCategory.MISC, builder -> builder
                    .noLootTable()
                    .sized(0.25F, 0.25F)
                    // Uzaktan da görünsün ve konumu istemciye sık gönderilsin ki ip titremesin.
                    .clientTrackingRange(8)
                    .updateInterval(2));

    public static final DeferredHolder<EntityType<?>, EntityType<IsaretFisegiEntity>> ISARET_FISEGI =
            ENTITIES.registerEntityType("isaret_fisegi", IsaretFisegiEntity::new, MobCategory.MISC, builder -> builder
                    .noLootTable()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(10)
                    .updateInterval(10));

    // Zipline hattının ucu: yerinde durur, çok nadiren güncellenmesi yeterli. Uzaktan da görünsün ki hat görünsün.
    public static final DeferredHolder<EntityType<?>, EntityType<ZiplineEntity>> ZIPLINE =
            ENTITIES.registerEntityType("zipline", ZiplineEntity::new, MobCategory.MISC, builder -> builder
                    .noLootTable()
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(Integer.MAX_VALUE));

    private ModEntities() {
    }
}
