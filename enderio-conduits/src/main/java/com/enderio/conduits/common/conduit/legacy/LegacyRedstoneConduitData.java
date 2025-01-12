package com.enderio.conduits.common.conduit.legacy;

import com.enderio.conduits.api.network.node.NodeData;
import com.enderio.conduits.api.network.node.legacy.ConduitData;
import com.enderio.conduits.api.network.node.legacy.ConduitDataType;
import com.enderio.conduits.common.init.ConduitTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

@Deprecated(forRemoval = true, since = "7.2")
public class LegacyRedstoneConduitData implements ConduitData<LegacyRedstoneConduitData> {

    public static MapCodec<LegacyRedstoneConduitData> CODEC = RecordCodecBuilder
            .mapCodec(
                    instance -> instance
                            .group(Codec.BOOL.fieldOf("is_active").forGetter(i -> i.isActive),
                                    Codec.unboundedMap(DyeColor.CODEC, Codec.INT)
                                            .fieldOf("active_colors")
                                            .forGetter(i -> i.activeColors))
                            .apply(instance, LegacyRedstoneConduitData::new));

    public static StreamCodec<RegistryFriendlyByteBuf, LegacyRedstoneConduitData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, r -> r.isActive,
            ByteBufCodecs.map(HashMap::new, DyeColor.STREAM_CODEC, ByteBufCodecs.INT), r -> r.activeColors,
            LegacyRedstoneConduitData::new);

    private boolean isActive = false;
    private final EnumMap<DyeColor, Integer> activeColors = new EnumMap<>(DyeColor.class);

    public LegacyRedstoneConduitData() {
    }

    private LegacyRedstoneConduitData(boolean isActive, Map<DyeColor, Integer> activeColors) {
        this.isActive = isActive;
        this.activeColors.putAll(activeColors);
    }

    @Override
    public ConduitDataType<LegacyRedstoneConduitData> type() {
        return ConduitTypes.Data.REDSTONE.get();
    }

    @Override
    public @Nullable NodeData toNodeData() {
        return null;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isActive(DyeColor color) {
        return activeColors.containsKey(color);
    }

    public int getSignal(DyeColor color) {
        return activeColors.getOrDefault(color, 0);
    }

    public Map<DyeColor, Integer> getActiveColors() {
        return activeColors;
    }

    public void clearActive() {
        activeColors.clear();
        isActive = false;
    }

    public void setActiveColor(DyeColor color, int signal) {
        if (activeColors.containsKey(color)) {
            return;
        }

        isActive = true;
        activeColors.put(color, signal);
    }

    @Override
    public LegacyRedstoneConduitData deepCopy() {
        return new LegacyRedstoneConduitData(isActive, new EnumMap<>(activeColors));
    }

    @Override
    public int hashCode() {
        return Objects.hash(isActive, activeColors);
    }
}
