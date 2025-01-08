package com.enderio.conduits.api.bundle;

import com.enderio.conduits.api.Conduit;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public sealed interface AddConduitResult permits AddConduitResult.Upgrade, AddConduitResult.Blocked, AddConduitResult.Insert{
     record Upgrade(Holder<Conduit<?>> replacedConduit) implements AddConduitResult {
        @Override
        public String toString() {
             return "Upgrade[" + replacedConduit.getRegisteredName() + "]";
         }
     }

    final class Insert implements AddConduitResult {
        @Override
        public String toString() {
            return "Insert";
        }
    }

    final class Blocked implements AddConduitResult {
        @Override
        public String toString() {
            return "Blocked";
        }
    }

    default boolean hasChanged() {
        return !(this instanceof Blocked);
    }
}
