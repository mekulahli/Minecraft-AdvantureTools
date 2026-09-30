package com.ismail.maceraaletleri.item;

import java.util.List;

import com.ismail.maceraaletleri.entity.KancaEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Sağ tık: kanca fırlatır. Kanca zaten dışarıdaysa ikinci sağ tık onu geri çeker.
public class KancaItem extends Item {
    private static final float FIRLATMA_GUCU = 2.5F;

    private final KancaSeviyesi seviye;

    public KancaItem(Item.Properties properties, KancaSeviyesi seviye) {
        super(properties);
        this.seviye = seviye;
    }

    public KancaSeviyesi getSeviye() {
        return seviye;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel) {
            List<KancaEntity> aktifKancalar = serverLevel.getEntitiesOfClass(
                    KancaEntity.class,
                    // Menzil III büyüsü menzili %60 artırabilir.
                    player.getBoundingBox().inflate(KancaEntity.menzil(seviye) * 1.6 + 8),
                    kanca -> kanca.getOwner() == player);

            if (!aktifKancalar.isEmpty()) {
                aktifKancalar.forEach(KancaEntity::discard);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1.0F, 1.2F);
                return InteractionResult.SUCCESS;
            }

            KancaEntity kanca = new KancaEntity(level, player, stack);
            kanca.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, FIRLATMA_GUCU, 0.5F);
            serverLevel.addFreshEntity(kanca);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 1.0F, 0.6F);

            // Her atış 1 dayanıklılık harcar (kreatif modda harcamaz).
            stack.hurtAndBreak(1, player, hand);
        }

        return InteractionResult.SUCCESS;
    }
}
