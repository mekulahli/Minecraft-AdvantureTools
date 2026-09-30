package com.ismail.maceraaletleri.item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.joml.Vector3f;

import com.ismail.maceraaletleri.Ayarlar;
import com.ismail.maceraaletleri.Basarimlar;
import com.mojang.math.Transformation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

// Sağ tık: etraftaki cevherleri birkaç saniyeliğine duvarların arkasından görünecek şekilde parlatır.
// Parlayan şey, cevherin içine konan küçük bir "block display" entity'sidir (vanilla'nın görüntü entity'si).
public class KasifDurbunuItem extends Item {
    public static final String PARLAMA_ETIKETI = "maceraaletleri_parlama";
    private static final int MAKS_CEVHER = 64;
    private static final int BEKLEME = 5 * 20;

    private record Parlama(Display.BlockDisplay entity, long bitisZamani) {
    }

    private static final List<Parlama> AKTIF_PARLAMALAR = new ArrayList<>();

    public KasifDurbunuItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer oyuncu)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = player.getItemInHand(hand);

        List<BlockPos> cevherler = cevherleriBul(serverLevel, player.blockPosition(), Ayarlar.DURBUN_YARICAP.get());
        long bitis = serverLevel.getGameTime() + Ayarlar.DURBUN_SURE.get() * 20L;
        boolean elmasVar = false;

        for (BlockPos pos : cevherler) {
            BlockState state = serverLevel.getBlockState(pos);
            elmasVar |= state.is(Tags.Blocks.ORES_DIAMOND);
            parlat(serverLevel, pos, state, bitis);
        }

        player.sendOverlayMessage(Component.translatable("mesaj.maceraaletleri.durbun", cevherler.size()));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        if (elmasVar) {
            Basarimlar.ver(oyuncu, Basarimlar.HAZINE_AVCISI);
        }

        player.getCooldowns().addCooldown(stack, BEKLEME);
        stack.hurtAndBreak(1, player, hand);
        return InteractionResult.SUCCESS;
    }

    private static List<BlockPos> cevherleriBul(ServerLevel level, BlockPos merkez, int yaricap) {
        List<BlockPos> bulunan = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(merkez.offset(-yaricap, -yaricap, -yaricap), merkez.offset(yaricap, yaricap, yaricap))) {
            if (level.getBlockState(pos).is(Tags.Blocks.ORES)) {
                bulunan.add(pos.immutable());
            }
        }
        bulunan.sort(Comparator.comparingDouble(pos -> pos.distSqr(merkez)));
        return bulunan.size() > MAKS_CEVHER ? bulunan.subList(0, MAKS_CEVHER) : bulunan;
    }

    private static void parlat(ServerLevel level, BlockPos pos, BlockState state, long bitis) {
        Display.BlockDisplay gorunum = new Display.BlockDisplay(EntityTypes.BLOCK_DISPLAY, level);
        gorunum.setBlockState(state);
        // Cevherin ortasında %60 boyutunda: gerçek blokla üst üste binip titremesin.
        gorunum.setTransformation(new Transformation(new Vector3f(0.2F, 0.2F, 0.2F), null, new Vector3f(0.6F, 0.6F, 0.6F), null));
        gorunum.setPos(pos.getX(), pos.getY(), pos.getZ());
        gorunum.setGlowingTag(true);
        gorunum.setGlowColorOverride(state.getMapColor(level, pos).col);
        gorunum.addTag(PARLAMA_ETIKETI);
        level.addFreshEntity(gorunum);
        AKTIF_PARLAMALAR.add(new Parlama(gorunum, bitis));
    }

    // Her sunucu tick'inde çağrılır: süresi dolan parlamaları kaldırır.
    public static void parlamalariTemizle() {
        AKTIF_PARLAMALAR.removeIf(p -> {
            boolean bitti = p.entity().isRemoved() || p.entity().level().getGameTime() >= p.bitisZamani();
            if (bitti) {
                p.entity().discard();
            }
            return bitti;
        });
    }

    // Oyun kapanırken kalan parlamalar dünyaya kaydedilebilir; yüklenince bunları tanıyıp siliyoruz.
    public static boolean artikParlamaMi(Display.BlockDisplay entity) {
        return entity.entityTags().contains(PARLAMA_ETIKETI)
                && AKTIF_PARLAMALAR.stream().noneMatch(p -> p.entity() == entity);
    }
}
