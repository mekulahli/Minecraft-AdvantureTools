package com.ismail.maceraaletleri.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ismail.maceraaletleri.ModItems;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

// İp merdivenin bir parçası. En üstteki parça arkasındaki duvara, alttakiler üsttekine asılıdır.
// Tırmanılabilir olması data/minecraft/tags/block/climbable.json ile sağlanır.
// Herhangi bir parçasını kırmak ya da Shift + sağ tık bütün merdiveni toplar ve 1 ip merdiven geri verir.
public class IpMerdivenBlock extends Block {
    public static final MapCodec<IpMerdivenBlock> CODEC = simpleCodec(IpMerdivenBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private static final Map<Direction, VoxelShape> SEKILLER = Shapes.rotateHorizontal(Block.boxZ(16.0, 13.0, 16.0));

    public IpMerdivenBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<IpMerdivenBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SEKILLER.get(state.getValue(FACING));
    }

    // Üstünde aynı yöne bakan merdiven varsa ona asılıdır; yoksa arkasındaki duvara tutunmalı.
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction yon = state.getValue(FACING);
        BlockState ust = level.getBlockState(pos.above());
        if (ust.is(this) && ust.getValue(FACING) == yon) {
            return true;
        }
        BlockPos duvar = pos.relative(yon.getOpposite());
        return level.getBlockState(duvar).isFaceSturdy(level, duvar, yon);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction komsuYonu, BlockPos komsuPos, BlockState komsu, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, ticks, pos, komsuYonu, komsuPos, komsu, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            topla(level, pos, state);
            if (!player.isCreative()) {
                Block.popResource(level, pos, new ItemStack(ModItems.IP_MERDIVEN.get()));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            topla(level, pos, state);
            level.removeBlock(pos, false);
            ItemStack merdiven = new ItemStack(ModItems.IP_MERDIVEN.get());
            if (!player.getInventory().add(merdiven)) {
                player.drop(merdiven, false);
            }
            level.playSound(null, pos, SoundEvents.LADDER_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    // Tıklanan parçanın üstündeki ve altındaki bütün parçaları kaldırır (tıklananın kendisi hariç).
    private void topla(Level level, BlockPos pos, BlockState state) {
        List<BlockPos> parcalar = new ArrayList<>();
        for (Direction yon : new Direction[]{Direction.UP, Direction.DOWN}) {
            BlockPos p = pos.relative(yon);
            while (level.getBlockState(p).is(this) && level.getBlockState(p).getValue(FACING) == state.getValue(FACING)) {
                parcalar.add(p);
                p = p.relative(yon);
            }
        }
        for (BlockPos p : parcalar) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(ModItems.IP_MERDIVEN.get());
    }
}
