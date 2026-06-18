package com.mudaemod.mudaemod.block;

import com.mudaemod.mudaemod.gui.MudaeMenu;
import com.mudaemod.mudaemod.network.handler.MudaeServerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.server.level.ServerPlayer;

public class MudaeBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape SHAPE = Block.box(2, 0, 4, 14, 16, 14);

    public MudaeBlock() {
        super(Properties.of()
            .strength(1.0f, 1.0f)
            .sound(SoundType.AMETHYST)
            .lightLevel(s -> 6)
            .noOcclusion());
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState()
            .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
            .setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level.isClientSide || state.getValue(HALF) != DoubleBlockHalf.LOWER) return;
        BlockPos above = pos.above();
        if (level.getBlockState(above).canBeReplaced()) {
            level.setBlock(above, defaultBlockState()
                .setValue(FACING, state.getValue(FACING))
                .setValue(HALF, DoubleBlockHalf.UPPER), 3);
        } else {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos other = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            BlockState otherState = level.getBlockState(other);
            if (otherState.is(this) && otherState.getValue(HALF) != half) {
                level.setBlock(other, Blocks.AIR.defaultBlockState(), 35);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        // Open menu only via the LOWER half to avoid double-open
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockPos below = pos.below();
            BlockState belowState = level.getBlockState(below);
            if (belowState.is(this)) {
                return belowState.useWithoutItem(level, player, new BlockHitResult(
                    hit.getLocation(), hit.getDirection(), below, hit.isInside()));
            }
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.2f);
            sp.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new MudaeMenu(id, inv),
                Component.literal("Terminal de Mudae")
            ));
            MudaeServerHandler.buildAndSendHarem(sp);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
