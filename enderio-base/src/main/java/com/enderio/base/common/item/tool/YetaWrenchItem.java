package com.enderio.base.common.item.tool;

import com.enderio.base.api.capability.SideConfig;
import com.enderio.base.common.blockentity.Wrenchable;
import com.enderio.base.common.init.EIOCapabilities;
import com.mojang.datafixers.util.Either;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class YetaWrenchItem extends Item {

    public YetaWrenchItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();

        // Check for side config capability
        SideConfig sideConfig = level.getCapability(EIOCapabilities.SideConfig.BLOCK, pos, pContext.getClickedFace());
        if (sideConfig != null) {
            sideConfig.cycleMode();
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // Look for rotation property
        BlockState state = level.getBlockState(pContext.getClickedPos());
        Optional<Either<DirectionProperty, EnumProperty<Direction.Axis>>> property = getRotationProperty(state);
        if (property.isPresent()) {
            BlockState newState = getNextState(pContext, state, property.get());
            pContext.getLevel()
                    .setBlock(pContext.getClickedPos(), newState, Block.UPDATE_NEIGHBORS + Block.UPDATE_CLIENTS);
            return InteractionResult.SUCCESS;
        }
        return super.onItemUseFirst(stack, pContext);
    }

    @SuppressWarnings("unchecked")
    private static Optional<Either<DirectionProperty, EnumProperty<Direction.Axis>>> getRotationProperty(
            BlockState state) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof DirectionProperty directionProperty
                    && directionProperty.getName().equals("facing")) {

                return Optional.of(Either.left(directionProperty));
            }
            if (property instanceof EnumProperty enumProperty && enumProperty.getName().equals("axis")
                    && enumProperty.getValueClass().equals(Direction.Axis.class)) {

                return Optional.of(Either.right(enumProperty));
            }
        }
        return Optional.empty();
    }

    private static BlockState getNextState(UseOnContext pContext, BlockState state,
            Either<DirectionProperty, EnumProperty<Direction.Axis>> property) {
        return handleProperties(pContext, state, property.left(), property.right());
    }

    private static BlockState handleProperties(UseOnContext pContext, BlockState state,
            Optional<DirectionProperty> directionProperty, Optional<EnumProperty<Direction.Axis>> axisProperty) {
        if (directionProperty.isPresent()) {
            return handleProperty(pContext, state, directionProperty.get());
        }

        if (axisProperty.isPresent()) {
            return handleProperty(pContext, state, axisProperty.get());
        }

        throw new IllegalArgumentException("At least one Optional should be set");
    }

    private static <T extends Comparable<T>> BlockState handleProperty(UseOnContext pContext, BlockState state,
            Property<T> property) {
        int noValidStateIndex = 0;
        do {
            state = getNextBlockState(state, property);
            noValidStateIndex++;
        } while (noValidStateIndex != property.getPossibleValues().size()
                && !state.canSurvive(pContext.getLevel(), pContext.getClickedPos()));

        return state;
    }

    private static <T extends Comparable<T>> BlockState getNextBlockState(BlockState currentState,
            Property<T> property) {
        return currentState.setValue(property, getNextValue(currentState.getValue(property), property));
    }

    private static <T extends Comparable<T>> T getNextValue(T value, Property<T> property) {
        boolean foundValid = false;
        for (T possibleValue : property.getPossibleValues()) {
            if (foundValid) {
                return possibleValue;
            }

            foundValid = possibleValue == value;
        }

        return property.getPossibleValues().iterator().next();
    }
}
