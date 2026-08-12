package top.xfunny.mixin;

import org.mtr.core.data.*;
import org.mtr.core.data.Lift;
import org.mtr.core.simulation.Simulator;
import org.mtr.core.tool.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import top.xfunny.mod.config.YteLiftConfigStore;

@Mixin(value = Lift.class, remap = false)
public abstract class MixinLift implements MixinLiftSchema, MixinLiftFields, MixinNameColorDataBaseSchema {

    /**
     * @author YTE
     * @reason Replace MAX_SPEED and ACCELERATION_DEFAULT with per-lift custom values
     */
    @Overwrite
    public void tick(long millisElapsed) {
        final long id = ((Lift) (Object) this).getId();
        final double customMaxSpeed = YteLiftConfigStore.getSpeed(id) / 1000.0;
        final double customAccel = YteLiftConfigStore.getAcceleration(id) / 1_000_000.0;

        if (getStoppingCoolDown() > 0) {
            setStoppingCoolDown(Math.max(getStoppingCoolDown() - millisElapsed, 0));
            if (getStoppingCoolDown() == 0) {
                if (isClientside()) {
                    setStoppingCoolDown(1);
                } else {
                    setNeedsUpdate(true);
                }
            }
        } else {
            if (getInstructions().isEmpty()) {
                setSpeed(Math.max(Math.abs(getSpeed()) - customAccel * millisElapsed, 0) * Math.signum(getSpeed()));
            } else {
                final long nextInstructionProgress = invokeGetProgress(getInstructions().get(0).getFloor());

                if (getSpeed() * getSpeed() / 2 / customAccel > Math.abs(nextInstructionProgress - getRailProgress())) {
                    setSpeed(Math.max(Math.abs(getSpeed()) - customAccel * millisElapsed, customAccel) * Math.signum(getSpeed()));
                } else {
                    setSpeed(Utilities.clamp(getSpeed() + customAccel * millisElapsed * Math.signum(nextInstructionProgress - getRailProgress()), -customMaxSpeed, customMaxSpeed));
                }

                if (Math.abs(getRailProgress() - nextInstructionProgress) <= Math.abs(getSpeed() * millisElapsed)) {
                    setRailProgress(nextInstructionProgress);
                    setSpeed(0);
                    if (!isClientside()) {
                        getInstructions().remove(0);
                        setStoppingCoolDown(Vehicle.DOOR_MOVE_TIME + 2500);
                        setNeedsUpdate(true);
                    }
                }
            }

            setRailProgress(Utilities.clamp(getRailProgress() + getSpeed() * millisElapsed, 0, invokeGetProgress(Integer.MAX_VALUE)));
        }

        if (getData() instanceof Simulator) {
            ((Simulator) getData()).clients.forEach(client -> {
                if (Utilities.isBetween(client.getPosition(), getMinPosition(), getMaxPosition(), client.getUpdateRadius())) {
                    client.update((Lift) (Object) this, getNeedsUpdate());
                }
            });

            setNeedsUpdate(false);
        }
    }
}