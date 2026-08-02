package top.xfunny.mod.client.screen.widget;

import org.jetbrains.annotations.NotNull;
import org.mtr.mapping.holder.MathHelper;
import org.mtr.mapping.holder.MutableText;
import org.mtr.mapping.mapper.ClickableWidgetExtension;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.GuiDrawing;
import top.xfunny.mod.client.screen.GuiHelper;

import java.util.ArrayList;
import java.util.List;

public class ListViewWidget extends ClickableWidgetExtension {
    public static final int ENTRY_PADDING = 5;
    public static final int SCROLLBAR_WIDTH = 5;
    private final List<BaseListItem> entryList = new ArrayList<>();
    protected double currentScroll = 0;
    private boolean scrollbarDragging = false;
    private int totalEntryHeight = 0;

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
        setScroll(currentScroll);
    }

    public void add(MutableText text, MappedWidget widget) {
        add(new ContentItem(text, widget));
    }

    public void add(BaseListItem listItem) {
        // O(1) 增量定位，renderContent 每帧会再次修正滚动位置
        listItem.positionChanged(getX2() + width - scrollbarWidth() - ENTRY_PADDING, getY2() + totalEntryHeight - (int) currentScroll);
        entryList.add(listItem);
        totalEntryHeight += listItem.height;
        setScroll(currentScroll);
    }

    public void addCategory(MutableText text) {
        add(new CategoryItem(text));
    }

    public void clear() {
        entryList.clear();
        totalEntryHeight = 0;
        setScroll(0);
    }

    @Override
    public void render(@NotNull GraphicsHolder graphicsHolder, int mouseX, int mouseY, float tickDelta) {
        GuiHelper.drawRectangle(new GuiDrawing(graphicsHolder), getX2(), getY2(), width, height, 0x4C4C4C4C);
        // vanilla 无 scissor 映射，经 GuiHelper 裁剪列表内容区（含滚动条）
        GuiHelper.enableScissor(graphicsHolder, getX2(), getY2(), getX2() + width, getY2() + height);
        try {
            renderContent(graphicsHolder, mouseX, mouseY, tickDelta);
            renderScrollBar(graphicsHolder, mouseX, mouseY, tickDelta);
        } finally {
            GuiHelper.disableScissor(graphicsHolder);
        }
    }

    public void renderContent(GraphicsHolder graphicsHolder, int mouseX, int mouseY, float tickDelta) {
        GuiDrawing guiDrawing = new GuiDrawing(graphicsHolder);
        int incY = 0;
        int scrollbarWidth = scrollbarWidth();
        int listItemWidth = width - scrollbarWidth;

        for (BaseListItem listItem : entryList) {
            int entryY = getY2() + incY - (int) currentScroll;
            // 内嵌控件先于本列表渲染、scissor 包不住，完全在可视区内才显示
            boolean fullyVisible = entryY >= getY2() && entryY + listItem.height <= getY2() + height;
            listItem.setWidgetVisible(fullyVisible);
            listItem.draw(graphicsHolder, guiDrawing, getX2(), entryY, listItemWidth, listItem.height, mouseX, mouseY, fullyVisible, tickDelta);
            listItem.positionChanged(getX2() + listItemWidth - ENTRY_PADDING, entryY);
            incY += listItem.height;
        }
    }

    @Override
    public boolean mouseScrolled2(double mouseX, double mouseY, double amount) {
        double oldScroll = currentScroll;
        if (contentOverflowed()) {
            amount *= 26;
            setScroll(oldScroll - amount);
        }
        return oldScroll != currentScroll;
    }

    @Override
    public boolean mouseClicked2(double mouseX, double mouseY, int button) {
        // 映射层转发不做命中判断，无条件返回 true 会吞掉之后添加的所有控件点击
        if (button == 0 && isScrollbarHover(mouseX, mouseY)) {
            scrollbarDragging = true;
            setScrollFromMouse(mouseY);
        }
        return isMouseOver2(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged2(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrollbarDragging) {
            setScrollFromMouse(mouseY);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased2(double mouseX, double mouseY, int button) {
        scrollbarDragging = false;
        return false;
    }

    private void setScrollFromMouse(double mouseY) {
        int entryHeight = totalEntryHeight;
        int visibleHeight = getHeight2();
        double scrollbarHeight = visibleHeight * ((double) visibleHeight / entryHeight);
        double trackHeight = visibleHeight - scrollbarHeight;
        if (trackHeight <= 0) {
            return;
        }
        setScroll((mouseY - getY2()) / trackHeight * (entryHeight - visibleHeight));
    }

    public void renderScrollBar(GraphicsHolder graphicsHolder, int mouseX, int mouseY, float tickDelta) {
        if (!contentOverflowed()) return;

        GuiDrawing guiDrawing = new GuiDrawing(graphicsHolder);
        int entryHeight = totalEntryHeight;
        int visibleHeight = getHeight2();
        // 计算滚动条滑块高度
        double scrollbarHeight = visibleHeight * ((double) visibleHeight / entryHeight);
        double bottomOffset = currentScroll / (entryHeight - visibleHeight);
        double yOffset = bottomOffset * (visibleHeight - scrollbarHeight);

        GuiHelper.drawRectangle(guiDrawing, getX2() + getWidth2() - SCROLLBAR_WIDTH, getY2() + yOffset, SCROLLBAR_WIDTH, scrollbarHeight, isScrollbarHover(mouseX, mouseY) ? 0xFFD1D1D1 : 0xFF9F9F9F);
    }

    protected boolean contentOverflowed() {
        return totalEntryHeight > getHeight2();
    }

    private int scrollbarWidth() {
        return contentOverflowed() ? SCROLLBAR_WIDTH : 0;
    }

    private boolean isScrollbarHover(double mouseX, double mouseY) {
        return mouseX >= getX2() + getWidth2() - SCROLLBAR_WIDTH && mouseY >= getY2() && mouseX < getX2() + getWidth2() && mouseY < getY2() + totalEntryHeight;
    }

    public void setScroll(double scroll) {
        int maxScroll = Math.max(0, totalEntryHeight - getHeight2());
        currentScroll = MathHelper.clamp(scroll, 0, maxScroll);
    }
}
