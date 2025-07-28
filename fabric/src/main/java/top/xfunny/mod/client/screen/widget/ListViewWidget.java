package top.xfunny.mod.client.screen.widget;

import org.jetbrains.annotations.NotNull;
import org.mtr.mapping.holder.MutableText;
import org.mtr.mapping.mapper.*;
import top.xfunny.mod.client.screen.GuiHelper;

import java.util.ArrayList;
import java.util.List;

public class ListViewWidget extends ClickableWidgetExtension {
    public static final int ENTRY_PADDING = 5;
    private final List<BaseListItem> displayedEntryList = new ArrayList<>();
    private final List<BaseListItem> entryList = new ArrayList<>();
    private String searchTerm = "";

    public ListViewWidget() {
        super(0, 0, 0, 0);
    }


    public ListViewWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public void setXYSize(int x, int y, int width, int height) {
        setX2(x);
        setY2(y);
        setWidth2(width);
        setHeightMapped(height);
        refreshDisplay();
    }

    public void add(MutableText text, ButtonWidgetExtension widget) {
        add(new ContentItem(text, widget));
        refreshDisplay();
    }

    public void add(BaseListItem listItem) {
        entryList.add(listItem);
        refreshDisplay();
    }

    public void addCategory(MutableText text) {
        entryList.add(new CategoryItem(text));
        refreshDisplay();
    }

    public void clear() {
        entryList.clear();
        refreshDisplay();
    }

    public void refreshDisplay() {
        displayedEntryList.addAll(entryList);
        entryList.clear();
        entryList.addAll(displayedEntryList);
        displayedEntryList.clear();
        updateItemPositions();
    }

    private void updateItemPositions() {
        int incY = 0;

        for (BaseListItem listItem : entryList) {
            int entryX = getX2();
            int entryY = getY2() + incY;
            listItem.positionChanged(entryX + width - ENTRY_PADDING, entryY);
            incY += listItem.height;
        }
    }

    @Override
    public void render(@NotNull GraphicsHolder graphicsHolder, int mouseX, int mouseY, float tickDelta) {
        GuiHelper.drawRectangle(new GuiDrawing(graphicsHolder), getX2(), getY2(), width, height, 0x4C4C4C4C);
        renderContent(graphicsHolder, mouseX, mouseY, tickDelta);
        super.render(graphicsHolder, mouseX, mouseY, tickDelta);
    }

    public void renderContent(GraphicsHolder graphicsHolder, int mouseX, int mouseY, float tickDelta){
        GuiDrawing guiDrawing = new GuiDrawing(graphicsHolder);
        int incY = 0;

        for (BaseListItem listItem : entryList) {
            int entryX = getX2();
            int entryY = getY2() + incY;
            listItem.draw(graphicsHolder,guiDrawing,entryX,entryY,width,height,mouseX,mouseY,true,tickDelta);
            listItem.positionChanged(entryX + width - ENTRY_PADDING, entryY);
            incY += listItem.height;
        }
    }

    //TODO: 添加滚动
}
