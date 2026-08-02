package top.xfunny.mod.client.screen;

import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.ButtonWidgetExtension;
import org.mtr.mapping.mapper.TextHelper;
import top.xfunny.mod.client.screen.base.BaseConfigScreen;
import top.xfunny.mod.client.screen.widget.ContentItem;
import top.xfunny.mod.client.screen.widget.MappedWidget;
/**
 * @deprecated 半成品
 */
@Deprecated
public class GuangzhouRailwaySignScreen extends BaseConfigScreen {
    private final ButtonWidgetExtension signSelected;
    protected String signId;

    public GuangzhouRailwaySignScreen(BlockPos blockPos) {
        super(blockPos);
        this.signId = null;

        signSelected = new ButtonWidgetExtension(0, 0, 60, 20, TextHelper.translatable("selectWorld.edit"), (btn) ->
                MinecraftClient.getInstance().openScreen(
                        new Screen(new SignSettingScreen(blockPos, signId, (str) ->
                                this.signId = str).withPreviousScreen(new Screen(this)))
                ));
    }

    public MutableText getScreenTitle() {
        return TextHelper.translatable("测试屏幕");
    }

    @Override
    public void addItemConfig() {
        addChild(new ClickableWidget(signSelected));
        // signId 未选择时为 null，translatable(null) 渲染即崩，空文本兜底
        ContentItem chooseSignItem = new ContentItem(signId == null ? TextHelper.literal("") : TextHelper.translatable(signId), new MappedWidget(signSelected));

        if (signId != null) {
            chooseSignItem.setIcon(new Identifier("mtr", "textures/block/sign/" + signId + ".png"));
        }

        listViewWidget.addCategory(TextHelper.translatable("gui.yte.signs.list.signs_setting"));
        listViewWidget.add(chooseSignItem);
        listViewWidget.addCategory(TextHelper.translatable("gui.yte.signs.list.signs_display"));
    }

}