package com.ismail.maceraaletleri.item;

import com.ismail.maceraaletleri.ModBlocks;
import com.ismail.maceraaletleri.block.IpMerdivenBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

// İp merdiven: bir duvarın yan yüzüne ya da uçurum kenarındaki bloğun üstüne sağ tıkla;
// merdiven aşağı doğru yere değene kadar (en fazla 24 blok) kendiliğinden açılır.
public class IpMerdivenItem extends Item {
    private static final int MAKS_UZUNLUK = 24;

    public IpMerdivenItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction yuz = context.getClickedFace();
        Direction yon;
        BlockPos baslangic;
        if (yuz.getAxis().isHorizontal()) {
            // Duvarın yan yüzü: merdiven duvarın önüne asılır.
            yon = yuz;
            baslangic = context.getClickedPos().relative(yuz);
        } else if (yuz == Direction.UP) {
            // Uçurum kenarı: bakılan yöndeki boşluğa, tıklanan bloğa tutunarak sarkar.
            yon = context.getHorizontalDirection();
            baslangic = context.getClickedPos().relative(yon);
        } else {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();
        BlockState parca = ModBlocks.IP_MERDIVEN.get().defaultBlockState().setValue(IpMerdivenBlock.FACING, yon);
        if (!level.getBlockState(baslangic).canBeReplaced() || !parca.canSurvive(level, baslangic)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        for (int i = 0; i < MAKS_UZUNLUK; i++) {
            BlockPos p = baslangic.below(i);
            if (!level.getBlockState(p).canBeReplaced()) {
                break;
            }
            level.setBlock(p, parca, 3);
        }
        level.playSound(null, baslangic, SoundEvents.LADDER_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
        context.getItemInHand().consume(1, context.getPlayer());
        return InteractionResult.SUCCESS;
    }
}
