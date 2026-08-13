package top.xfunny.core.data;

import org.mtr.core.serializer.ReaderBase;
import top.xfunny.core.generated.data.YteLiftConfigSchema;

public class YteLiftConfig extends YteLiftConfigSchema {

    public YteLiftConfig(long liftId) {
        super(liftId, DEFAULT_SPEED, DEFAULT_ACCELERATION, DEFAULT_ADO_DISTANCE, DEFAULT_LEVELLING_DISTANCE, DEFAULT_LEVELLING_SPEED);
    }

    public YteLiftConfig(long liftId, double speed, double acceleration, double adoDistance, double levellingDistance, double levellingSpeed) {
        super(liftId, speed, acceleration, adoDistance, levellingDistance, levellingSpeed);
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

    public double getAdoDistance() { return adoDistance; }

    public double getLevellingDistance() { return levellingDistance; }

    public double getLevellingSpeed() { return levellingSpeed; }

    public void setSpeed(double speed) {
        this.speed = clamp(speed, MIN_SPEED, MAX_SPEED);
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = clamp(acceleration, MIN_ACCELERATION, MAX_ACCELERATION);
    }

    public void setAdoDistance(double adoDistance) { this.adoDistance = clamp(adoDistance, MIN_ADO_DISTANCE, MAX_ADO_DISTANCE); }

    public void setLevellingDistance(double levellingDistance) { this.levellingDistance = clamp(levellingDistance, MIN_LEVELLING_DISTANCE, MAX_LEVELLING_DISTANCE); }

    public void setLevellingSpeed(double levellingSpeed) { this.levellingSpeed = clamp(levellingSpeed, MIN_LEVELLING_SPEED, MAX_LEVELLING_SPEED); }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
