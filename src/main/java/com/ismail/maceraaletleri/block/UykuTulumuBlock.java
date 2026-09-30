package com.ismail.maceraaletleri.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

// Uyku tulumu: yere serilir, sağ tıkla uyunur. Oyun onu yatak sayar (isBed), böylece gece atlanır;
// ama doğma noktası değişmez (SunucuOlaylari#onSetSpawn). Kırınca kendisi düşer.
public class UykuTulumuBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<UykuTulumuBlock> CODEC = simpleCodec(UykuTulumuBlock::new);
    private static final VoxelShape SEKIL = Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0);

    public UykuTulumuBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    public MapCodec<UykuTulumuBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SEKIL;
    }

    // Baş ucu oyuncunun baktığı yöne gelir.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public boolean isBed(BlockState state, BlockGetter level, BlockPos pos, LivingEntity sleeper) {
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer oyuncu) {
            oyuncu.startSleepInBed(pos).ifLeft(sorun -> {
                if (sorun.message() != null) {
                    oyuncu.sendOverlayMessage(sorun.message());
                }
            });
        }
        return InteractionResult.SUCCESS;
    }
}
