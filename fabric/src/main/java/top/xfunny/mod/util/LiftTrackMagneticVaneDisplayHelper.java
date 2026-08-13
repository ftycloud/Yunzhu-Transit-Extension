package top.xfunny.mod.util;

import org.mtr.core.data.Lift;
import org.mtr.core.data.LiftFloor;
import org.mtr.mapping.holder.BlockEntity;
import org.mtr.mapping.holder.BlockPos;
import org.mtr.mapping.holder.World;
import top.xfunny.mixin.MixinLiftFields;
import top.xfunny.mixin.MixinLiftSchema;
import top.xfunny.mod.Init;
import top.xfunny.mod.block.LiftTrackMagneticVane;

public final class LiftTrackMagneticVaneDisplayHelper {

    private LiftTrackMagneticVaneDisplayHelper() {
    }

    public static LiftTrackMagneticVane.BlockEntity getDisplayedMagneticVane(World world, Lift lift) {
        final MixinLiftSchema schema = (MixinLiftSchema) lift;
        final MixinLiftFields fields = (MixinLiftFields) lift;
        final double railProgress = schema.getRailProgress();

        for (int index = 1; index < schema.getFloors().size(); index++) {
            final LiftFloor lowerFloor = schema.getFloors().get(index - 1);
            final LiftFloor upperFloor = schema.getFloors().get(index);
            final long lowerProgress = fields.invokeGetProgress(index - 1);
            final long upperProgress = fields.invokeGetProgress(index);
            if (railProgress < Math.min(lowerProgress, upperProgress) || railProgress > Math.max(lowerProgress, upperProgress)) {
                continue;
            }
            final long lowerY = lowerFloor.getPosition().getY();
            final long upperY = upperFloor.getPosition().getY();
            if (lowerY == upperY) {
                continue;
            }

            final long minY = Math.min(lowerY, upperY);
            final long maxY = Math.max(lowerY, upperY);
            final long x = lowerFloor.getPosition().getX();
            final long z = lowerFloor.getPosition().getZ();
            LiftTrackMagneticVane.BlockEntity nearestVane = null;
            double nearestVaneDistance = Double.POSITIVE_INFINITY;

            for (long y = minY + 1; y < maxY; y++) {
                final BlockPos blockPos = Init.positionToBlockPos(new org.mtr.core.data.Position(x, y, z));
                final BlockEntity blockEntity = world.getBlockEntity(blockPos);
                if (blockEntity == null || !(blockEntity.data instanceof LiftTrackMagneticVane.BlockEntity)) {
                    continue;
                }

                final double percentage = (double) (y - lowerY) / (upperY - lowerY);
                final double markerProgress = lowerProgress + (upperProgress - lowerProgress) * percentage;
                final double markerDistance = Math.abs(markerProgress - railProgress);
                if (markerDistance < nearestVaneDistance) {
                    nearestVane = (LiftTrackMagneticVane.BlockEntity) blockEntity.data;
                    nearestVaneDistance = markerDistance;
                }
            }

            final double nearestRealFloorDistance = Math.min(
                    Math.abs(railProgress - lowerProgress),
                    Math.abs(railProgress - upperProgress));
            return nearestVaneDistance < nearestRealFloorDistance ? nearestVane : null;
        }

        return null;
    }
}
