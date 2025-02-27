package com.enderio.machines.common.blocks.wireless_charger;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

public class WirelessAntennaBlock extends Block {

    private static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final IntegerProperty WIRELESS_RANGE = IntegerProperty.create("wireless_charger_range", 0, 2048);

    private final ModConfigSpec.ConfigValue<Integer> rangeExtension;

    public WirelessAntennaBlock(Properties pProperties, ModConfigSpec.ConfigValue<Integer> rangeExtension) {
        super(pProperties);
        BlockState any = this.getStateDefinition().any();
        this.registerDefaultState(any.hasProperty(FACING) ? any.setValue(FACING, Direction.NORTH) : any);
        this.rangeExtension = rangeExtension;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WIRELESS_RANGE);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(WIRELESS_RANGE, rangeExtension.get());
    }

}
