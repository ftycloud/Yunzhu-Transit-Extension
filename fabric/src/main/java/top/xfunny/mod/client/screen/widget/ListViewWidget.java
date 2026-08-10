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
    private double scrollbarDragOffset = 0;
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
            // 只要 Item 和列表视口有交集就显示其控件，交给 scissor 裁切，而不是整颗消失
            // TODO: GuiHelper 在 1.19.2 及更早可能无 scissor 并永久降级，此时半截 item 的控件会画出列表外；
            //       需要暴露 GuiHelper.isScissorAvailable()，不可用时对内嵌控件回退到 fullyVisible 才渲染。
            boolean partiallyVisible = entryY + listItem.height > getY2() && entryY < getY2() + height;
            listItem.setWidgetVisible(partiallyVisible);
            if (partiallyVisible) {
                listItem.positionChanged(getX2() + listItemWidth - ENTRY_PADDING, entryY);
                listItem.draw(graphicsHolder, guiDrawing, getX2(), entryY, listItemWidth, listItem.height, mouseX, mouseY, true, tickDelta);
            }
            incY += listItem.height;
        }
    }

    // TODO: 鼠标不在列表区域内时不应滚动。Screen 会向所有 child 广播滚轮事件，这里应加 isMouseOver2(mouseX, mouseY) 守卫。
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
        // 只有点在滚动条 thumb 上才开启拖拽，并记录 grab offset，避免 thumb 跳到鼠标中心
        if (button == 0 && contentOverflowed() && isScrollbarThumbHover(mouseX, mouseY)) {
            scrollbarDragging = true;
            scrollbarDragOffset = mouseY - getScrollbarThumbY();
            return true;
        }

        // 内嵌控件不再作为 Screen child，由 ListViewWidget 手动分发点击
        // TODO: 目前对 entryList 全量分发，依赖底层 mouseClicked2 自行检查 visible/active；
        //       更稳的做法是与 renderContent 用同一套 entryY/partiallyVisible 计算，只分发给当前可见 item。
        if (isMouseOver2(mouseX, mouseY)) {
            for (BaseListItem listItem : entryList) {
                if (listItem instanceof ContentItem) {
                    MappedWidget widget = ((ContentItem) listItem).widget;
                    if (widget != null && widget.mouseClicked(mouseX, mouseY, button)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged2(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (scrollbarDragging) {
            setScrollFromThumbPosition(mouseY - scrollbarDragOffset);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased2(double mouseX, double mouseY, int button) {
        scrollbarDragging = false;
        return false;
    }

    private void setScrollFromThumbPosition(double thumbTop) {
        int visibleHeight = getHeight2();
        double scrollbarHeight = getScrollbarThumbHeight();
        double trackHeight = visibleHeight - scrollbarHeight;
        if (trackHeight <= 0) {
            return;
        }
        setScroll((thumbTop - getY2()) / trackHeight * (totalEntryHeight - visibleHeight));
    }

    public void renderScrollBar(GraphicsHolder graphicsHolder, int mouseX, int mouseY, float tickDelta) {
        if (!contentOverflowed()) return;

        GuiDrawing guiDrawing = new GuiDrawing(graphicsHolder);
        double thumbY = getScrollbarThumbY();
        double thumbHeight = getScrollbarThumbHeight();

        GuiHelper.drawRectangle(guiDrawing, getX2() + getWidth2() - SCROLLBAR_WIDTH, thumbY, SCROLLBAR_WIDTH, thumbHeight, isScrollbarThumbHover(mouseX, mouseY) ? 0xFFD1D1D1 : 0xFF9F9F9F);
    }

    protected boolean contentOverflowed() {
        return totalEntryHeight > getHeight2();
    }

    private int scrollbarWidth() {
        return contentOverflowed() ? SCROLLBAR_WIDTH : 0;
    }

    private double getScrollbarThumbHeight() {
        int visibleHeight = getHeight2();
        return visibleHeight * ((double) visibleHeight / totalEntryHeight);
    }

    private double getScrollbarThumbY() {
        int visibleHeight = getHeight2();
        double thumbHeight = getScrollbarThumbHeight();
        double bottomOffset = currentScroll / (totalEntryHeight - visibleHeight);
        return getY2() + bottomOffset * (visibleHeight - thumbHeight);
    }

    private boolean isScrollbarThumbHover(double mouseX, double mouseY) {
        if (!contentOverflowed()) {
            return false;
        }
        double thumbY = getScrollbarThumbY();
        double thumbHeight = getScrollbarThumbHeight();
        return mouseX >= getX2() + getWidth2() - SCROLLBAR_WIDTH
                && mouseY >= thumbY
                && mouseX < getX2() + getWidth2()
                && mouseY < thumbY + thumbHeight;
    }

    public void setScroll(double scroll) {
        int maxScroll = Math.max(0, totalEntryHeight - getHeight2());
        currentScroll = MathHelper.clamp(scroll, 0, maxScroll);
    }
}
