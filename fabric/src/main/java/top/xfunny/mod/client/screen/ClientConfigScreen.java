package top.xfunny.mod.client.screen;

import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.*;

import org.mtr.mod.client.IDrawing;

import org.mtr.mod.data.IGui;
import org.mtr.mod.generated.lang.TranslationProvider;
import org.mtr.mod.screen.MTRScreenBase;
import top.xfunny.mod.client.InitClient;

import javax.annotation.Nullable;

public class ClientConfigScreen extends MTRScreenBase implements IGui {
    private final ButtonWidgetExtension hideTestWatermark;

    private static final int BUTTON_WIDTH = 60;
    private static final int BUTTON_HEIGHT = TEXT_HEIGHT * 2;

    public ClientConfigScreen(@Nullable Screen previousScreen) {
        super(previousScreen);

        hideTestWatermark = new ButtonWidgetExtension(0, 0, 0, BUTTON_HEIGHT, TextHelper.literal(""), button -> {
            InitClient.getConfig().hideTestWatermark = !InitClient.getConfig().hideTestWatermark;
            setButtonText(button, InitClient.getConfig().hideTestWatermark);
        });
    }

    @Override
    protected void init2() {
        super.init2();

        int startY = TEXT_PADDING;
        int i = 1;

        IDrawing.setPositionAndWidth(hideTestWatermark, width - SQUARE_SIZE - BUTTON_WIDTH, startY + BUTTON_HEIGHT * (i++) + SQUARE_SIZE, BUTTON_WIDTH);
        setButtonText(new ButtonWidget(hideTestWatermark), InitClient.getConfig().hideTestWatermark);

        // 添加控件到界面
        addChild(new ClickableWidget(hideTestWatermark));
    }

    @Override
    public void render(GraphicsHolder graphicsHolder, int mouseX, int mouseY, float delta) {
        try {
            renderBackground(graphicsHolder);

            // 绘制标题和选项标签
            final int yStart = SQUARE_SIZE + TEXT_PADDING + TEXT_PADDING / 2;
            int i = 1;
            graphicsHolder.drawText(TextHelper.literal("Hide Test Watermark"), SQUARE_SIZE, BUTTON_HEIGHT * (i++) + yStart, ARGB_WHITE, false, GraphicsHolder.getDefaultLight());

            super.render(graphicsHolder, mouseX, mouseY, delta);
        } catch (Exception e) {
            // 记录错误日志
        }
    }

    @Override
    public void onClose2() {
        super.onClose2();
        InitClient.getConfig().writeConfig();
    }

    private static void setButtonText(ButtonWidget button, boolean state) {
        button.setMessage((state ? TranslationProvider.OPTIONS_MTR_ON : TranslationProvider.OPTIONS_MTR_OFF).getText());
    }
}
