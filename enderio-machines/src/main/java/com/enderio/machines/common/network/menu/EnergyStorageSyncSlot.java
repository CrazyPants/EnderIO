package com.enderio.machines.common.network.menu;

import com.enderio.core.common.network.menu.SyncSlot;
import com.enderio.core.common.network.menu.payload.IntSlotPayload;
import com.enderio.core.common.network.menu.payload.ListSlotPayload;
import com.enderio.core.common.network.menu.payload.PairSlotPayload;
import com.enderio.core.common.network.menu.payload.SlotPayload;
import com.enderio.core.common.network.menu.payload.SlotPayloadType;
import com.enderio.machines.common.blocks.base.energy.EnergyStorageInfo;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.world.level.Level;

public abstract class EnergyStorageSyncSlot implements SyncSlot {

    public static EnergyStorageSyncSlot standalone() {
        return new EnergyStorageSyncSlot() {
            private EnergyStorageInfo value = new EnergyStorageInfo(0, 0, 0, 0);

            @Override
            public EnergyStorageInfo get() {
                return value;
            }

            @Override
            public void set(EnergyStorageInfo value) {
                this.value = value;
            }
        };
    }

    public static EnergyStorageSyncSlot simple(Supplier<EnergyStorageInfo> getter, Consumer<EnergyStorageInfo> setter) {
        return new EnergyStorageSyncSlot() {

            @Override
            public EnergyStorageInfo get() {
                return getter.get();
            }

            @Override
            public void set(EnergyStorageInfo value) {
                setter.accept(value);
            }
        };
    }

    public static EnergyStorageSyncSlot readOnly(Supplier<EnergyStorageInfo> getter) {
        return new EnergyStorageSyncSlot() {

            @Override
            public EnergyStorageInfo get() {
                return getter.get();
            }

            @Override
            public void set(EnergyStorageInfo value) {
                throw new UnsupportedOperationException("Attempt to set a read-only sync slot.");
            }
        };
    }

    private EnergyStorageInfo lastValue;

    public abstract EnergyStorageInfo get();

    public abstract void set(EnergyStorageInfo value);

    @Override
    public ChangeType detectChanges() {
        var currentValue = get();
        if (Objects.equals(lastValue, currentValue)) {
            return ChangeType.NONE;
        }

//        var changeType = lastValue == null || currentValue.maxEnergyStored() != lastValue.maxEnergyStored()
//                ? ChangeType.FULL
//                : ChangeType.PARTIAL;
//        lastValue = currentValue;
//        return changeType;
        return ChangeType.FULL;
    }

    @Override
    public SlotPayload createPayload(Level level, ChangeType changeType) {
        var value = get();

        if (changeType == ChangeType.PARTIAL) {
            return new IntSlotPayload(value.energyStored());
        }

//        return new PairSlotPayload(new IntSlotPayload(value.energyStored()),
//                new IntSlotPayload(value.maxEnergyStored()));
        return new ListSlotPayload(List.of(new IntSlotPayload(value.energyStored()),
                            new IntSlotPayload(value.maxEnergyStored()),
            new IntSlotPayload(value.energyReceived()),
            new IntSlotPayload(value.energyUsed())));
    }

    @Override
    public void unpackPayload(Level level, SlotPayload payload) {
        if (payload instanceof IntSlotPayload intSlotPayload) {
            set(get().withEnergyStored(intSlotPayload.value()));
        } else if (payload instanceof PairSlotPayload pairSlotPayload) {
            if (pairSlotPayload.left().type() != SlotPayloadType.INT
                    || pairSlotPayload.right().type() != SlotPayloadType.INT) {
                return;
            }

            set(new EnergyStorageInfo(((IntSlotPayload) pairSlotPayload.left()).value(),
                    ((IntSlotPayload) pairSlotPayload.right()).value(), 0, 0));
        } else if (payload instanceof ListSlotPayload listSlotPayload) {
//            if (pairSlotPayload.left().type() != SlotPayloadType.INT
//                || pairSlotPayload.right().type() != SlotPayloadType.INT) {
//                return;
//            }
            List<SlotPayload> vals = listSlotPayload.contents();
            if(vals.size() != 4) {
                return;
            }
            EnergyStorageInfo esi = new EnergyStorageInfo(((IntSlotPayload) vals.get(0)).value(), ((IntSlotPayload) vals.get(1)).value(),
                ((IntSlotPayload) vals.get(2)).value(), ((IntSlotPayload) vals.get(3)).value());
            set(esi);
        }
    }
}
