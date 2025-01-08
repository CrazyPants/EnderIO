package com.enderio.conduits.common.conduit.graph;

import com.enderio.conduits.api.Conduit;
import com.enderio.conduits.api.network.ConduitNetworkContext;
import com.enderio.conduits.api.network.ConduitNetworkContextAccessor;
import com.enderio.conduits.api.network.ConduitNetworkContextType;
import com.enderio.conduits.api.EnderIOConduitsRegistries;
import dev.gigaherz.graph3.Mergeable;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class ConduitGraphContext implements Mergeable<ConduitGraphContext>, ConduitNetworkContextAccessor {

    @Nullable
    private ConduitNetworkContext<?> context;

    public ConduitGraphContext() {
    }

    private ConduitGraphContext(@Nullable ConduitNetworkContext<?> context) {
        this.context = context;
    }

    @Override
    public boolean hasContext(ConduitNetworkContextType<?> type) {
        return context != null && context.type() == type;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ConduitNetworkContext<T>> @Nullable T getContext(ConduitNetworkContextType<T> type) {
        if (context != null && context.type() == type) {
            return (T) context;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ConduitNetworkContext<T>> T getOrCreateContext(ConduitNetworkContextType<T> type) {
        if (context != null && context.type() == type) {
            return (T) context;
        }

        context = type.factory().get();
        return (T) context;
    }

    // region Mergable Implementation

    @Override
    public ConduitGraphContext mergeWith(ConduitGraphContext other) {
        if (context != null && other.context != null) {
            return new ConduitGraphContext(context.mergeWith(other.castContext()));
        } else if (context == null && other.context != null) {
            return new ConduitGraphContext(other.context);
        } else if (context != null) {
            return new ConduitGraphContext(context);
        }

        return this;
    }

    @Override
    public ConduitGraphContext copy() {
        if (context == null) {
            return this;
        }

        return new ConduitGraphContext(context.copy());
    }

    // endregion

    private <Z extends ConduitNetworkContext<Z>> Z castContext() {
        // noinspection unchecked
        return (Z) context;
    }

    @Nullable
    public CompoundTag save(HolderLookup.Provider lookupProvider) {
        if (context == null) {
            return null;
        }

        if (context.type().codec() == null) {
            return null;
        }

        var contextTypeKey = Objects.requireNonNull(
                EnderIOConduitsRegistries.CONDUIT_NETWORK_CONTEXT_TYPE.getKey(context.type()),
                "Context type is not registered!");

        var tag = new CompoundTag();
        tag.putString("Type", contextTypeKey.toString());
        tag.put("Data", encodeContext(lookupProvider, context.type()));
        return tag;
    }

    // Gross.
    @SuppressWarnings("unchecked")
    private <T extends ConduitNetworkContext<T>> Tag encodeContext(HolderLookup.Provider lookupProvider,
            ConduitNetworkContextType<T> type) {
        return type.codec()
                .encodeStart(lookupProvider.createSerializationContext(NbtOps.INSTANCE), (T) context)
                .getOrThrow();
    }

    public static <T extends ConduitNetworkContext<T>> ConduitGraphContext of(T context) {
        return new ConduitGraphContext(context);
    }

    public static ConduitGraphContext createNetworkContext() {
        return new ConduitGraphContext();
    }

    public static ConduitGraphContext loadNetworkContext(Holder<Conduit<?>> conduit,
            HolderLookup.Provider lookupProvider, CompoundTag contextTag) {
        ConduitNetworkContext<?> context = loadNetworkContext(conduit.value(), lookupProvider, contextTag);
        return new ConduitGraphContext(context);
    }

    @Nullable
    private static ConduitNetworkContext<?> loadNetworkContext(Conduit<?> conduit, HolderLookup.Provider lookupProvider,
            CompoundTag contextTag) {
        ResourceLocation serializerKey = ResourceLocation.parse(contextTag.getString("Type"));
        ConduitNetworkContextType<?> contextType = Objects.requireNonNull(
                EnderIOConduitsRegistries.CONDUIT_NETWORK_CONTEXT_TYPE.get(serializerKey),
                "Unable to find conduit network context type with key " + serializerKey);

        if (contextType.codec() == null) {
            return null;
        }

        // TODO: We're using getOrThrow a lot for conduits. Should definitely make more
        // robust/flexible.
        CompoundTag data = contextTag.getCompound("Data");
        return contextType.codec().parse(lookupProvider.createSerializationContext(NbtOps.INSTANCE), data).getOrThrow();
    }
}
