package com.enderio.conduits.common.conduit.connection;

import com.enderio.base.api.UseOnly;
import com.enderio.base.api.misc.RedstoneControl;
import com.enderio.base.api.network.MassiveStreamCodec;
import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.bundle.SlotType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;

@Deprecated(forRemoval = true, since = "7.2")
public record DynamicConnectionState(boolean isInsert, DyeColor insertChannel, boolean isExtract,
        DyeColor extractChannel, RedstoneControl control, DyeColor redstoneChannel,
        @UseOnly(LogicalSide.SERVER) ItemStack filterInsert, @UseOnly(LogicalSide.SERVER) ItemStack filterExtract,
        @UseOnly(LogicalSide.SERVER) ItemStack upgradeExtract) implements ConnectionState {

    public static Codec<DynamicConnectionState> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Codec.BOOL.fieldOf("is_insert").forGetter(DynamicConnectionState::isInsert),
                    DyeColor.CODEC.fieldOf("insert_channel").forGetter(DynamicConnectionState::insertChannel),
                    Codec.BOOL.fieldOf("is_extract").forGetter(DynamicConnectionState::isExtract),
                    DyeColor.CODEC.fieldOf("extract_channel").forGetter(DynamicConnectionState::extractChannel),
                    RedstoneControl.CODEC.fieldOf("redstone_control").forGetter(DynamicConnectionState::control),
                    DyeColor.CODEC.fieldOf("redstone_channel").forGetter(DynamicConnectionState::redstoneChannel),
                    ItemStack.OPTIONAL_CODEC.fieldOf("filter_insert").forGetter(DynamicConnectionState::filterInsert),
                    ItemStack.OPTIONAL_CODEC.fieldOf("filter_extract").forGetter(DynamicConnectionState::filterExtract),
                    ItemStack.OPTIONAL_CODEC.fieldOf("upgrade_extract")
                            .forGetter(DynamicConnectionState::upgradeExtract))
            .apply(instance, DynamicConnectionState::new));

    public static StreamCodec<RegistryFriendlyByteBuf, DynamicConnectionState> STREAM_CODEC = MassiveStreamCodec
            .composite(ByteBufCodecs.BOOL, DynamicConnectionState::isInsert, DyeColor.STREAM_CODEC,
                    DynamicConnectionState::insertChannel, ByteBufCodecs.BOOL, DynamicConnectionState::isExtract,
                    DyeColor.STREAM_CODEC, DynamicConnectionState::extractChannel, RedstoneControl.STREAM_CODEC,
                    DynamicConnectionState::control, DyeColor.STREAM_CODEC, DynamicConnectionState::redstoneChannel,
                    ItemStack.OPTIONAL_STREAM_CODEC, DynamicConnectionState::filterInsert,
                    ItemStack.OPTIONAL_STREAM_CODEC, DynamicConnectionState::filterExtract,
                    ItemStack.OPTIONAL_STREAM_CODEC, DynamicConnectionState::upgradeExtract,
                    DynamicConnectionState::new);

    public static DynamicConnectionState defaultConnection(Level level, BlockPos pos, Direction direction,
            Holder<Conduit<?, ?>> type) {
        // Conduit.ConduitConnectionData defaultConnection =
        // type.value().getDefaultConnection(level, pos, direction);
        // return new DynamicConnectionState(defaultConnection.isInsert(),
        // DyeColor.GREEN, defaultConnection.isExtract(), DyeColor.GREEN,
        // defaultConnection.control(), DyeColor.RED, ItemStack.EMPTY, ItemStack.EMPTY,
        // ItemStack.EMPTY);
        // STUB
        return new DynamicConnectionState(false, DyeColor.GREEN, true, DyeColor.GREEN, RedstoneControl.NEVER_ACTIVE,
                DyeColor.RED, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    @Override
    public boolean isConnection() {
        return true;
    }

    public ItemStack getItem(SlotType slotType) {
        if (slotType == SlotType.FILTER_EXTRACT) {
            return filterExtract;
        }

        if (slotType == SlotType.FILTER_INSERT) {
            return filterInsert;
        }

        return upgradeExtract;
    }

    public DynamicConnectionState withItem(SlotType type, ItemStack stack) {
        Map<SlotType, ItemStack> items = new HashMap<>();
        for (SlotType type1 : SlotType.values()) {
            items.put(type1, type1 == type ? stack : getItem(type1));
        }
        return new DynamicConnectionState(isInsert, insertChannel, isExtract, extractChannel, control, redstoneChannel,
                items.get(SlotType.FILTER_INSERT), items.get(SlotType.FILTER_EXTRACT),
                items.get(SlotType.UPGRADE_EXTRACT));
    }

    public DynamicConnectionState withEnabled(boolean forExtract, boolean value) {
        return new DynamicConnectionState(!forExtract ? value : isInsert, insertChannel, forExtract ? value : isExtract,
                extractChannel, control, redstoneChannel, filterInsert, filterExtract, upgradeExtract);
    }

    public DynamicConnectionState withColor(boolean forExtract, DyeColor value) {
        return new DynamicConnectionState(isInsert, !forExtract ? value : insertChannel, isExtract,
                forExtract ? value : extractChannel, control, redstoneChannel, filterInsert, filterExtract,
                upgradeExtract);
    }

    public DynamicConnectionState withRedstoneMode(RedstoneControl value) {
        return new DynamicConnectionState(isInsert, insertChannel, isExtract, extractChannel, value, redstoneChannel,
                filterInsert, filterExtract, upgradeExtract);
    }

    public DynamicConnectionState withRedstoneChannel(DyeColor value) {
        return new DynamicConnectionState(isInsert, insertChannel, isExtract, extractChannel, control, value,
                filterInsert, filterExtract, upgradeExtract);
    }

    public boolean isEmpty() {
        return !isInsert && !isExtract;
    }
}
