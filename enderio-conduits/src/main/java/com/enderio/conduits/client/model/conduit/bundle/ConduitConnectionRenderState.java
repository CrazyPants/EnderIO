package com.enderio.conduits.client.model.conduit.bundle;

import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.connection.config.ConnectionConfig;
import com.enderio.conduits.api.connection.config.NewIOConnectionConfig;
import com.enderio.conduits.api.connection.config.RedstoneSensitiveConnectionConfig;
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
    boolean isRedstoneSensitive,
    DyeColor redstoneChannel
) {

    public static ConduitConnectionRenderState fake() {
        return new ConduitConnectionRenderState(false, DyeColor.GREEN, false, DyeColor.GREEN, false, DyeColor.RED);
    }

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
            }
        } else if (connectionConfig instanceof NewIOConnectionConfig ioConnectionConfig) {
            // TODO: Tidy the language here.
            canInput = ioConnectionConfig.isSend();
            canOutput = ioConnectionConfig.isReceive();
            inputChannel = ioConnectionConfig.sendColor();
            outputChannel = ioConnectionConfig.receiveColor();
        }

        boolean isRedstoneSensitive = false;
        DyeColor redstoneChannel = DyeColor.RED;

        if (connectionConfig instanceof RedstoneControlledConnection redstoneControlledConnection) {
            if (redstoneControlledConnection.redstoneControl().isRedstoneSensitive()) {
                isRedstoneSensitive = true;
                redstoneChannel = redstoneControlledConnection.redstoneChannel();
            }
        } else if (connectionConfig instanceof RedstoneSensitiveConnectionConfig redstoneSensitiveConfig) {
            // TODO: Support for multiple colours
            var channelColors = redstoneSensitiveConfig.getRedstoneSignalColors();
            if (!channelColors.isEmpty()) {
                isRedstoneSensitive = true;
                redstoneChannel = channelColors.getFirst();
            }
        }

        return new ConduitConnectionRenderState(
            canInput,
            inputChannel,
            canOutput,
            outputChannel,
            isRedstoneSensitive,
            redstoneChannel
        );
    }
}
