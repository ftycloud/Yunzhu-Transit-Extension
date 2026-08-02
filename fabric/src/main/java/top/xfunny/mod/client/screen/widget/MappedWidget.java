package top.xfunny.mod.client.screen.widget;

import org.mtr.mapping.mapper.*;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MappedWidget {
    private final Object widget;
    // ponytail: 5 个 MTR 映射基类方法签名完全一致（getX2/setWidth2/setVisibleMapped/...），
    // 反射 + 方法缓存替代 6 分支 instanceof 样板；方法缺失时静默返回默认值
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    public MappedWidget(Object widget) {
        while (widget instanceof MappedWidget) {
            widget = ((MappedWidget) widget).widget;
        }
        if (!(widget instanceof ButtonWidgetExtension || widget instanceof TextFieldWidgetExtension ||
                widget instanceof CheckboxWidgetExtension || widget instanceof SliderWidgetExtension ||
                widget instanceof ClickableWidgetExtension)) {
            throw new IllegalArgumentException("Unsupported widget type: " + (widget == null ? "null" : widget.getClass().getName()));
        }
        this.widget = widget;
    }

    public int getX() {
        return getInt("getX2");
    }

    public int getY() {
        return getInt("getY2");
    }

    public int getWidth() {
        return getInt("getWidth2");
    }

    public int getHeight() {
        return getInt("getHeight2");
    }

    public boolean getActive() {
        final Object value = invoke("getActiveMapped");
        return !(value instanceof Boolean) || (Boolean) value;
    }

    public void setWidth(int width) {
        invoke("setWidth2", new Class[]{int.class}, width);
    }

    public void setX(int newX) {
        invoke("setX2", new Class[]{int.class}, newX);
    }

    public void setY(int newY) {
        invoke("setY2", new Class[]{int.class}, newY);
    }

    public void render(GraphicsHolder graphicsHolder, int mouseX, int mouseY, float tickDelta) {
        invoke("render", new Class[]{GraphicsHolder.class, int.class, int.class, float.class}, graphicsHolder, mouseX, mouseY, tickDelta);
    }

    public void setVisible(boolean value) {
        invoke("setVisibleMapped", new Class[]{boolean.class}, value);
    }

    private int getInt(String name) {
        final Object value = invoke(name);
        return value instanceof Integer ? (Integer) value : 0;
    }

    private Object invoke(String name) {
        return invoke(name, new Class<?>[0]);
    }

    private Object invoke(String name, Class<?>[] parameterTypes, Object... args) {
        final Method method = METHOD_CACHE.computeIfAbsent(name, n -> {
            try {
                return widget.getClass().getMethod(n, parameterTypes);
            } catch (NoSuchMethodException e) {
                return null;
            }
        });
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(widget, args);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
