package com.ismail.maceraaletleri.item;

import java.util.function.Consumer;

import com.ismail.maceraaletleri.Ayarlar;
import com.ismail.maceraaletleri.ModDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

// Shift + sağ tık: bulunduğun yeri kaydeder. Sağ tık: kayıtlı yere ışınlar (bekleme süresi var).
public class IsinlanmaPusulasiItem extends Item {
    public IsinlanmaPusulasiItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer oyuncu) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            BlockPos konum = player.blockPosition();
            stack.set(ModDataComponents.KAYITLI_KONUM.get(), GlobalPos.of(level.dimension(), konum));
            player.sendOverlayMessage(Component.translatable("mesaj.maceraaletleri.pusula_kaydedildi",
                    konum.getX(), konum.getY(), konum.getZ()));
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResult.SUCCESS;
        }

        GlobalPos kayit = stack.get(ModDataComponents.KAYITLI_KONUM.get());
        if (kayit == null) {
            player.sendOverlayMessage(Component.translatable("mesaj.maceraaletleri.pusula_bos"));
            return InteractionResult.FAIL;
        }

        ServerLevel hedefDunya = serverLevel.getServer().getLevel(kayit.dimension());
        if (hedefDunya == null || (hedefDunya != serverLevel && !Ayarlar.PUSULA_BOYUTLAR_ARASI.get())) {
            player.sendOverlayMessage(Component.translatable("mesaj.maceraaletleri.pusula_boyut"));
            return InteractionResult.FAIL;
        }

        partikulVeSes(serverLevel, player.position());
        Vec3 hedef = Vec3.atBottomCenterOf(kayit.pos());
        oyuncu.teleport(new TeleportTransition(hedefDunya, hedef, Vec3.ZERO,
                player.getYRot(), player.getXRot(), TeleportTransition.DO_NOTHING));
        oyuncu.resetFallDistance();
        partikulVeSes(hedefDunya, hedef);

        player.getCooldowns().addCooldown(stack, Ayarlar.PUSULA_BEKLEME.get() * 20);
        stack.hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS;
    }

    private static void partikulVeSes(ServerLevel level, Vec3 konum) {
        level.sendParticles(ParticleTypes.PORTAL, konum.x, konum.y + 1, konum.z, 40, 0.5, 1.0, 0.5, 0.5);
        level.playSound(null, konum.x, konum.y, konum.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    // Kayıtlı konum varsa üstüne gelince göster.
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        GlobalPos kayit = stack.get(ModDataComponents.KAYITLI_KONUM.get());
        if (kayit == null) {
            builder.accept(Component.translatable("tooltip.maceraaletleri.pusula_bos").withStyle(ChatFormatting.GRAY));
        } else {
            BlockPos p = kayit.pos();
            builder.accept(Component.translatable("tooltip.maceraaletleri.pusula_konum",
                    p.getX(), p.getY(), p.getZ(), kayit.dimension().identifier().getPath()).withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(ModDataComponents.KAYITLI_KONUM.get());
    }
}
