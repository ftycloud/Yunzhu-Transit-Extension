package top.xfunny.core.generated.data;

import org.mtr.core.serializer.ReaderBase;
import org.mtr.core.serializer.SerializedDataBaseWithId;
import org.mtr.core.serializer.WriterBase;

public abstract class YteLiftConfigSchema implements SerializedDataBaseWithId {

    protected long liftId;
    protected double speed;
    protected double acceleration;

    private static final String KEY_LIFT_ID = "lift_id";
    private static final String KEY_SPEED = "speed";
    private static final String KEY_ACCELERATION = "acceleration";

    public static final double DEFAULT_SPEED = 10.0;
    public static final double DEFAULT_ACCELERATION = 4.0;
    public static final double MIN_SPEED = 0.5;
    public static final double MAX_SPEED = 20.0;
    public static final double MIN_ACCELERATION = 0.5;
    public static final double MAX_ACCELERATION = 10.0;
    public static final double STEP = 0.5;

    protected YteLiftConfigSchema(long liftId, double speed, double acceleration) {
        this.liftId = liftId;
        this.speed = speed;
        this.acceleration = acceleration;
    }

    protected YteLiftConfigSchema(ReaderBase readerBase) {
        updateData(readerBase);
    }

    @Override
    public void updateData(ReaderBase readerBase) {
        readerBase.unpackLong(KEY_LIFT_ID, value -> liftId = value);
        readerBase.unpackDouble(KEY_SPEED, value -> speed = value);
        readerBase.unpackDouble(KEY_ACCELERATION, value -> acceleration = value);
    }

    @Override
    public void serializeData(WriterBase writerBase) {
        writerBase.writeLong(KEY_LIFT_ID, liftId);
        writerBase.writeDouble(KEY_SPEED, speed);
        writerBase.writeDouble(KEY_ACCELERATION, acceleration);
    }

    @Override
    public String getHexId() {
        return Long.toHexString(liftId);
    }

    @Override
    public boolean isValid() {
        return liftId != 0
                && speed >= MIN_SPEED && speed <= MAX_SPEED
                && acceleration >= MIN_ACCELERATION && acceleration <= MAX_ACCELERATION;
    }

    public long getId() {
        return liftId;
    }
}
