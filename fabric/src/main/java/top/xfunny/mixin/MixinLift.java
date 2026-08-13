package top.xfunny.mixin;

import org.mtr.core.data.*;
import org.mtr.core.data.Lift;
import org.mtr.core.simulation.Simulator;
import org.mtr.core.tool.Utilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.xfunny.mod.config.YteLiftConfigStore;
import top.xfunny.mod.Init;

@Mixin(value = Lift.class, remap = false)
public abstract class MixinLift implements MixinLiftSchema, MixinLiftFields, MixinNameColorDataBaseSchema {

    @Unique
    private static final long YTE_LIFT_STOPPING_TIME = Vehicle.DOOR_MOVE_TIME + 2500;

    @Unique
    private static final long YTE_BRAKE_HOLD_TIME = 200;

    /**
     * MTR's door curve becomes negative when the cooldown is extended beyond its
     * native stopping time. Clamp that short brake-hold section to fully closed.
     */
    @Inject(method = "getDoorValue", at = @At("RETURN"), cancellable = true)
    private void yte$clampBrakeHoldDoorValue(CallbackInfoReturnable<Float> cir) {
        if (cir.getReturnValue() < 0) {
            cir.setReturnValue(0F);
        }
    }

    /**
     * @author YTE
     * @reason Replace MAX_SPEED and ACCELERATION_DEFAULT with per-lift custom values
     */
    @Overwrite
    public void tick(long millisElapsed) {
        final long id = ((Lift) (Object) this).getId();
        final double customMaxSpeed = YteLiftConfigStore.getSpeed(id) / 1000.0;
        final double customAccel = YteLiftConfigStore.getAcceleration(id) / 1_000_000.0;
        final double adoDistance = YteLiftConfigStore.getAdoDistance(id);
        final double levellingDistance = YteLiftConfigStore.getLevellingDistance(id);
        final double levellingSpeed = YteLiftConfigStore.getLevellingSpeed(id) / 1000.0;

        final boolean adoLevelling = getStoppingCoolDown() > 0 && getSpeed() != 0 && !getInstructions().isEmpty();

        if (getStoppingCoolDown() > 0 && !adoLevelling) {
            setStoppingCoolDown(Math.max(getStoppingCoolDown() - millisElapsed, 0));
            if (getStoppingCoolDown() == 0) {
                if (isClientside()) {
                    setStoppingCoolDown(1);
                } else {
                    setNeedsUpdate(true);
                }
            }
        } else {
            if (adoLevelling) {
                setStoppingCoolDown(Math.max(getStoppingCoolDown() - millisElapsed, 0));
            }

            if (getInstructions().isEmpty()) {
                setSpeed(Math.max(Math.abs(getSpeed()) - customAccel * millisElapsed, 0) * Math.signum(getSpeed()));
            } else {
                final long nextInstructionProgress = invokeGetProgress(getInstructions().get(0).getFloor());

                if (getSpeed() * getSpeed() / 2 / customAccel > Math.abs(nextInstructionProgress - getRailProgress())) {
                    setSpeed(Math.max(Math.abs(getSpeed()) - customAccel * millisElapsed, customAccel) * Math.signum(getSpeed()));
                } else {
                    setSpeed(Utilities.clamp(getSpeed() + customAccel * millisElapsed * Math.signum(nextInstructionProgress - getRailProgress()), -customMaxSpeed, customMaxSpeed));
                }

                final double distanceToTarget = Math.abs(nextInstructionProgress - getRailProgress());
                if (getSpeed() != 0 && levellingDistance > 0 && levellingSpeed > 0 && distanceToTarget <= levellingDistance) {
                    final double levellingDeceleration = levellingSpeed * levellingSpeed / (2 * levellingDistance);
                    final double levellingTargetSpeed = Math.sqrt(2 * levellingDeceleration * distanceToTarget);
                    setSpeed(Math.min(Math.abs(getSpeed()), levellingTargetSpeed) * Math.signum(getSpeed()));
                }

                final double movementThisTick = Math.abs(getSpeed() * millisElapsed);
                if (adoDistance > 0 && !isClientside() && !adoLevelling && getSpeed() != 0 && distanceToTarget <= adoDistance + movementThisTick) {
                    setStoppingCoolDown(YTE_LIFT_STOPPING_TIME);
                    Init.sendLiftAdoStart(id, YTE_LIFT_STOPPING_TIME);
                }

                if (Math.abs(getRailProgress() - nextInstructionProgress) <= Math.abs(getSpeed() * millisElapsed)) {
                    setRailProgress(nextInstructionProgress);
                    setSpeed(0);
                    if (!isClientside()) {
                        getInstructions().remove(0);
                        if (getStoppingCoolDown() == 0) {
                            setStoppingCoolDown(YTE_LIFT_STOPPING_TIME + (adoDistance <= 0 ? YTE_BRAKE_HOLD_TIME : 0));
                        }
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
