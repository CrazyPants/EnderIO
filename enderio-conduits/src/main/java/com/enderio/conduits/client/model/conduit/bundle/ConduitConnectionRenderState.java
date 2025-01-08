package com.enderio.conduits.client.model.conduit.bundle;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.io.ChanneledIOConnectionConfig;
import com.enderio.conduits.api.connection.config.io.IOConnectionConfig;
import com.enderio.conduits.api.connection.config.redstone.RedstoneControlledConnection;
import com.enderio.conduits.api.model.ConduitModelModifier;
import com.enderio.conduits.client.model.conduit.modifier.ConduitModelModifiers;
import me.liliandev.ensure.ensures.EnsureSide;
import net.minecraft.core.Holder;
import net.minecraft.world.item.DyeColor;

public record ConduitConnectionRenderState(
    boolean canInput,
    DyeColor inputChannel,
    boolean canOutput,
    DyeColor outputChannel,
    RedstoneControl redstoneControl,
    DyeColor redstoneChannel
) {

    @EnsureSide(EnsureSide.Side.CLIENT)
    public static ConduitConnectionRenderState of(Holder<Conduit<?, ?>> conduit, ConnectionConfig connectionConfig) {
        boolean canInput = false;
        boolean canOutput = false;
        DyeColor inputChannel = DyeColor.GREEN;
        DyeColor outputChannel = DyeColor.GREEN;
        if (connectionConfig instanceof IOConnectionConfig ioConnectionConfig) {
            canInput = ioConnectionConfig.canInsert();
            canOutput = ioConnectionConfig.canExtract();

            if (ioConnectionConfig instanceof ChanneledIOConnectionConfig channeledIOConnectionConfig) {
                inputChannel = channeledIOConnectionConfig.insertChannel();
                outputChannel = channeledIOConnectionConfig.extractChannel();
            } else {
                ConduitModelModifier conduitModelModifier = ConduitModelModifiers
                    .getModifier(conduit.value().type());

                if (conduitModelModifier != null) {
                    inputChannel = conduitModelModifier.getDefaultArrowColor();
                    outputChannel = conduitModelModifier.getDefaultArrowColor();
                }
            }
        }

        RedstoneControl redstoneControl = RedstoneControl.ALWAYS_ACTIVE;
        DyeColor redstoneChannel = DyeColor.RED;

        if (connectionConfig instanceof RedstoneControlledConnection redstoneControlledConnection) {
            redstoneControl = redstoneControlledConnection.redstoneControl();
            redstoneChannel = redstoneControlledConnection.redstoneChannel();
        }

        return new ConduitConnectionRenderState(
            canInput,
            inputChannel,
            canOutput,
            outputChannel,
            redstoneControl,
            redstoneChannel
        );
    }
}
