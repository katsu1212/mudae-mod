package com.mudaemod.mudaemod.block;

import com.mudaemod.mudaemod.gui.MudaeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class MudaeBlock extends Block {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public MudaeBlock() {
        super(Properties.of()
            .strength(3.5f, 6.0f)
            .sound(SoundType.AMETHYST)
            .lightLevel(s -> 7)
            .requiresCorrectToolForDrops());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.0f);
            sp.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new MudaeMenu(id, inv),
                Component.literal("🌸 Altar de Mudae")
            ));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
