package top.xfunny.mod.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局电梯配置存储，供 Mixin 快速访问
 * 由 YteSimulator (服务端) 和 YteData (客户端) 各自填充
 */
public final class YteLiftConfigStore {

    private static final Map<Long, Double> speedMap = new ConcurrentHashMap<>();
    private static final Map<Long, Double> accelerationMap = new ConcurrentHashMap<>();

    private static final double DEFAULT_SPEED = 10.0;
    private static final double DEFAULT_ACCELERATION = 4.0;

    private YteLiftConfigStore() {}

    public static void put(long liftId, double speed, double acceleration) {
        speedMap.put(liftId, speed);
        accelerationMap.put(liftId, acceleration);
    }

    public static double getSpeed(long liftId) {
        return speedMap.getOrDefault(liftId, DEFAULT_SPEED);
    }

    public static double getAcceleration(long liftId) {
        return accelerationMap.getOrDefault(liftId, DEFAULT_ACCELERATION);
    }

    public static void remove(long liftId) {
        speedMap.remove(liftId);
        accelerationMap.remove(liftId);
    }

    public static void clear() {
        speedMap.clear();
        accelerationMap.clear();
    }
}
