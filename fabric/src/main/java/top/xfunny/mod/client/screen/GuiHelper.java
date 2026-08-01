package top.xfunny.mod.client.screen;

import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.GuiDrawing;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class GuiHelper {
    public static final int MAX_CONTENT_WIDTH = 400;

    private static Field drawContextField;
    private static Method enableScissorMethod;
    private static Method disableScissorMethod;
    private static boolean scissorUnavailable;

    private GuiHelper() {
    }

    public static void drawRectangle(GuiDrawing guiDrawing, double x, double y, double width, double height, int color) {
        guiDrawing.beginDrawingRectangle();
        guiDrawing.drawRectangle(x, y, x + width, y + height, color);
        guiDrawing.finishDrawingRectangle();
    }

    // ponytail: 全反射访问 MTR 未暴露的 drawContext（包私有字段），不直接 import vanilla 类：
    // fabric 是 Yarn 名（DrawContext）、forge 是 Mojmap 名（GuiGraphics），且 1.19.2 及更早版本无 scissor API。
    // 按运行时类型与方法名查找，任何一步失败即永久降级为无裁剪（与 MTR 原版行为一致）。
    public static void enableScissor(GraphicsHolder graphicsHolder, int x1, int y1, int x2, int y2) {
        final Object drawContext = getDrawContext(graphicsHolder);
        if (enableScissorMethod == null && !scissorUnavailable && drawContextField != null) {
            try {
                enableScissorMethod = drawContextField.getType().getMethod("enableScissor", int.class, int.class, int.class, int.class);
            } catch (NoSuchMethodException e) {
                scissorUnavailable = true;
            }
        }
        if (drawContext == null || enableScissorMethod == null) {
            return;
        }
        try {
            enableScissorMethod.invoke(drawContext, x1, y1, x2, y2);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static void disableScissor(GraphicsHolder graphicsHolder) {
        final Object drawContext = getDrawContext(graphicsHolder);
        if (disableScissorMethod == null && !scissorUnavailable && drawContextField != null) {
            try {
                disableScissorMethod = drawContextField.getType().getMethod("disableScissor");
            } catch (NoSuchMethodException e) {
                scissorUnavailable = true;
            }
        }
        if (drawContext == null || disableScissorMethod == null) {
            return;
        }
        try {
            disableScissorMethod.invoke(drawContext);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static Object getDrawContext(GraphicsHolder graphicsHolder) {
        if (scissorUnavailable) {
            return null;
        }
        if (drawContextField == null) {
            try {
                drawContextField = GraphicsHolder.class.getDeclaredField("drawContext");
                drawContextField.setAccessible(true);
            } catch (NoSuchFieldException | SecurityException e) {
                scissorUnavailable = true;
                return null;
            }
        }
        try {
            return drawContextField.get(graphicsHolder);
        } catch (IllegalAccessException e) {
            scissorUnavailable = true;
            return null;
        }
    }
}
