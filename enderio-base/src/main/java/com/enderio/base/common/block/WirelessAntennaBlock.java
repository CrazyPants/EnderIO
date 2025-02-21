package com.enderio.base.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class WirelessAntennaBlock extends Block {

    private static final MapCodec<WirelessAntennaBlock> CODEC = simpleCodec(WirelessAntennaBlock::new);

    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public WirelessAntennaBlock(Properties pProperties) {
        super(pProperties);
        BlockState any = this.getStateDefinition().any();
        this.registerDefaultState(any.hasProperty(FACING) ? any.setValue(FACING, Direction.NORTH) : any);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected MapCodec<WirelessAntennaBlock> codec() {
        return CODEC;
    }
}
