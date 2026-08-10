package top.xfunny.mod.client.screen.base;

import org.jetbrains.annotations.NotNull;
import org.mtr.mapping.holder.BlockPos;
import org.mtr.mapping.holder.ClickableWidget;
import org.mtr.mapping.holder.MutableText;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.TextHelper;
import top.xfunny.mod.client.screen.widget.ListViewWidget;

import static top.xfunny.mod.client.screen.GuiHelper.MAX_CONTENT_WIDTH;

public abstract class BaseConfigScreen extends TitledScreen {
    protected final ListViewWidget listViewWidget;
    final BlockPos blockPos;

    public BaseConfigScreen(BlockPos blockPos) {
        this.blockPos = blockPos;
        this.listViewWidget = new ListViewWidget();
    }

    @Override
    protected void init2() {
        super.init2();
        int contentWidth = (int) Math.min((width * 0.75), MAX_CONTENT_WIDTH);
        int listViewHeight = (int) ((height - 60) * 0.754);
        int startX = (width - contentWidth) / 2;
        int startY = TEXT_PADDING * 5;

        listViewWidget.clear();
        listViewWidget.setXYSize(startX, startY, contentWidth, listViewHeight);
//        listViewWidget.active = false;
        // 列表行内控件已不再作为 Screen child，由 ListViewWidget 统一渲染和分发点击，这里只需添加列表本身
        addChild(new ClickableWidget(listViewWidget));
        addItemConfig();
    }

    @Override
    public void render(@NotNull GraphicsHolder graphicsHolder, int mouseX, int mouseY, float tickDelta) {
        renderBackground(graphicsHolder);
        super.render(graphicsHolder, mouseX, mouseY, tickDelta);
    }

    public MutableText getScreenSubtitle() {
        if (blockPos == null) {
            return TextHelper.literal("");
        }
        return TextHelper.translatable("gui.yte.subtitle", blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public abstract void addItemConfig();
}
