package top.xfunny.mod.client.screen;

import org.mtr.mapping.mapper.GuiDrawing;

public interface GuiHelper {
    int MAX_CONTENT_WIDTH = 400;

    static void drawRectangle(GuiDrawing guiDrawing, double x, double y, double width, double height, int color) {
        guiDrawing.beginDrawingRectangle();
        guiDrawing.drawRectangle(x, y, x + width, y + height, color);
        guiDrawing.finishDrawingRectangle();
    }
}
