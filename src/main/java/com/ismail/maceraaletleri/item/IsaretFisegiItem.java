package com.ismail.maceraaletleri.item;

import com.ismail.maceraaletleri.entity.IsaretFisegiEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// Sağ tık: işaret fişeğini fırlatır. Düştüğü yerde bir dakika ışık ve kırmızı duman verir.
public class IsaretFisegiItem extends Item {
    public IsaretFisegiItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            IsaretFisegiEntity fisek = new IsaretFisegiEntity(serverLevel, player, stack);
            fisek.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            serverLevel.addFreshEntity(fisek);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.5F, 1.8F);
        }
        player.getCooldowns().addCooldown(stack, 10);
        stack.consume(1, player);
        return InteractionResult.SUCCESS;
    }
}
