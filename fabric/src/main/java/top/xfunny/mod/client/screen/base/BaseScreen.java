package top.xfunny.mod.client.screen.base;

import org.mtr.mapping.holder.MinecraftClient;
import org.mtr.mapping.holder.Screen;
import org.mtr.mapping.mapper.ScreenExtension;
import org.mtr.mod.data.IGui;
import top.xfunny.mod.client.screen.GuiHelper;

public abstract class BaseScreen extends ScreenExtension implements IGui {
    private Screen previousScreen = null;

    public BaseScreen() {
        super();
    }

    public BaseScreen withPreviousScreen(Screen screen) {
        this.previousScreen = screen;
        return this;
    }

    @Override
    protected void init2() {
        super.init2();
        // MTR mapping 层不会自动调用 Screen.init()，子类每次 init2() 添加的控件会累积
        GuiHelper.clearScreenChildren(this);
    }

    @Override
    public void onClose2() {
        super.onClose2(); // vanilla Screen.onClose（setScreen(null)），补全 removed 关闭链
        MinecraftClient.getInstance().openScreen(previousScreen);
    }
}
