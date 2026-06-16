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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.server.level.ServerPlayer;

public class MudaeBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Shapes orientadas al norte — se rotan automáticamente en getShape
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
        Block.box(3, 0, 1, 13, 2, 6),   // base
        Block.box(6, 2, 2, 10, 7, 4),   // cuello
        Block.box(2, 7, 0, 14, 16, 5)   // monitor
    );

    public MudaeBlock() {
        super(Properties.of()
            .strength(3.5f, 6.0f)
            .sound(SoundType.AMETHYST)
            .lightLevel(s -> 6)
            .noOcclusion()
            .requiresCorrectToolForDrops());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> Shapes.or(
                Block.box(3, 0, 10, 13, 2, 15),
                Block.box(6, 2, 12, 10, 7, 14),
                Block.box(2, 7, 11, 14, 16, 16)
            );
            case EAST -> Shapes.or(
                Block.box(10, 0, 3, 15, 2, 13),
                Block.box(12, 2, 6, 14, 7, 10),
                Block.box(11, 7, 2, 16, 16, 14)
            );
            case WEST -> Shapes.or(
                Block.box(1, 0, 3, 6, 2, 13),
                Block.box(2, 2, 6, 4, 7, 10),
                Block.box(0, 7, 2, 5, 16, 14)
            );
            default -> SHAPE_NORTH;
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
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
