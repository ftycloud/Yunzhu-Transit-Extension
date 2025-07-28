package top.xfunny.mod.client.screen;

import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.mapper.GuiDrawing;

public interface GuiHelper {
    int MAX_CONTENT_WIDTH = 400;

    static void drawTexture(GuiDrawing guiDrawing, Identifier identifier, double x, double y, double width, double height, float u1, float v1, float u2, float v2) {
        guiDrawing.beginDrawingTexture(identifier);
        guiDrawing.drawTexture(x, y, x + width, y + height, u1, v1, u2, v2);
        guiDrawing.finishDrawingTexture();
    }

    static void drawRectangle(GuiDrawing guiDrawing, double x, double y, double width, double height, int color) {
        guiDrawing.beginDrawingRectangle();
        guiDrawing.drawRectangle(x, y, x + width, y + height, color);
        guiDrawing.finishDrawingRectangle();
    }
}
