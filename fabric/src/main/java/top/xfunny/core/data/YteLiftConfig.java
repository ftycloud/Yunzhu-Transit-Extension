package top.xfunny.core.data;

import org.mtr.core.serializer.ReaderBase;
import top.xfunny.core.generated.data.YteLiftConfigSchema;

public class YteLiftConfig extends YteLiftConfigSchema {

    public YteLiftConfig(long liftId) {
        super(liftId, DEFAULT_SPEED, DEFAULT_ACCELERATION);
    }

    public YteLiftConfig(long liftId, double speed, double acceleration) {
        super(liftId, speed, acceleration);
    }

    public YteLiftConfig(ReaderBase readerBase) {
        super(readerBase);
    }

    public double getSpeed() {
        return speed;
    }

    public double getAcceleration() {
        return acceleration;
    }

    public void setSpeed(double speed) {
        this.speed = clamp(speed, MIN_SPEED, MAX_SPEED);
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = clamp(acceleration, MIN_ACCELERATION, MAX_ACCELERATION);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
