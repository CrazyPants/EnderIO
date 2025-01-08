package com.enderio.conduits.client.model.conduit.bundle;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.io.ResourceConnectionConfig;
import com.enderio.conduits.api.connection.config.redstone.RedstoneControlledConnection;
import net.minecraft.world.item.DyeColor;

public record ConduitConnectionRenderState(
    boolean canInput,
    DyeColor inputChannel,
    boolean canOutput,
    DyeColor outputChannel,
    RedstoneControl redstoneControl,
    DyeColor redstoneChannel
) {
    public static ConduitConnectionRenderState of(ConnectionConfig connectionConfig) {
        boolean canInput = false;
        boolean canOutput = false;
        DyeColor inputChannel = DyeColor.GREEN;
        DyeColor outputChannel = DyeColor.GREEN;
        if (connectionConfig instanceof ResourceConnectionConfig resourceConnectionConfig) {
            canInput = resourceConnectionConfig.canInsert();
            canOutput = resourceConnectionConfig.canExtract();
            inputChannel = resourceConnectionConfig.insertChannel();
            outputChannel = resourceConnectionConfig.extractChannel();
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
