package top.xfunny.mod.client.screen;

import org.mtr.mapping.holder.Direction;
import org.mtr.mapping.mapper.GraphicsHolder;

public interface RenderHelper {
    int ARGB_WHITE = 0xFFFFFFFF;
    int lineHeight = 9;

    static void scaleToFit(GraphicsHolder graphicsHolder, int targetW, double maxW, boolean keepAspectRatio) {
        scaleToFit(graphicsHolder, targetW, maxW, keepAspectRatio, 0);
    }

    static void scaleToFit(GraphicsHolder graphicsHolder, double targetW, double maxW, boolean keepAspectRatio, double height) {
        height = height / 2;
        double scaleX = Math.min(1, maxW / targetW);
        if(scaleX < 1) {
            if(keepAspectRatio) {
                graphicsHolder.translate(0, height / 2.0, 0);
                graphicsHolder.scale((float)scaleX, (float)scaleX, (float)scaleX);
                graphicsHolder.translate(0, -height / 2.0, 0);
            } else {
                graphicsHolder.scale((float)scaleX, 1, 1);
            }
        }
    }

    static void drawTexture(GraphicsHolder graphicsHolder, float x, float y, float z, float width, float height, float u1, float v1, float u2, float v2, Direction facing, int color, int light) {
        drawTextureRaw(graphicsHolder, x, y, z, x + width, y + height, z, u1, v1, u2, v2, facing, color, light);
    }

    static void drawTexture(GraphicsHolder graphicsHolder, float x, float y, float z, float width, float height, Direction facing, int color, int light) {
        drawTextureRaw(graphicsHolder, x, y, z, x + width, y + height, z, 0, 0, 1, 1, facing, color, light);
    }

    static void drawTextureRaw(GraphicsHolder graphicsHolder, float x1, float y1, float z1, float x2, float y2, float z2, float u1, float v1, float u2, float v2, Direction facing, int color, int light) {
        graphicsHolder.drawTextureInWorld(x1, y2, z1, x2, y2, z2, x2, y1, z2, x1, y1, z1, u1, v1, u2, v2, facing, color, light);
    }
}