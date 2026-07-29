package top.xfunny.mod.block.base;

/**
 * 电梯相对于终端的位置方向。
 * <p>
 * 终端面向的方向为"前"，反方向为"后"。
 * 结合左右，共四个方位。
 */
public enum LiftRelativePosition {
    /** 左前方：&lt;A */
    FRONT_LEFT("<", ""),
    /** 右前方：A&gt; */
    FRONT_RIGHT("", ">"),
    /** 左后方：■A */
    BACK_LEFT("■", ""),
    /** 右后方：A■ */
    BACK_RIGHT("", "■"),
    /** 未知 / 无法判断 */
    UNKNOWN("?", "");

    public final String leftSymbol;
    public final String rightSymbol;

    LiftRelativePosition(String leftSymbol, String rightSymbol) {
        this.leftSymbol = leftSymbol;
        this.rightSymbol = rightSymbol;
    }

    /**
     * 将电梯编号格式化为带方向指示的字符串。
     * 例如 FRONT_LEFT.format("A") → "&lt;A"
     */
    public String format(String identifier) {
        return leftSymbol + identifier + rightSymbol;
    }
}
