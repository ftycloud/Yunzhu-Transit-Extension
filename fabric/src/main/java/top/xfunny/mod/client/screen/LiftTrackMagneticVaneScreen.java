package top.xfunny.mod.client.screen;

import org.jetbrains.annotations.NotNull;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.ScreenExtension;
import org.mtr.mapping.mapper.TextFieldWidgetExtension;
import org.mtr.mapping.tool.TextCase;
import org.mtr.mod.client.IDrawing;
import org.mtr.mod.data.IGui;
import org.mtr.mod.generated.lang.TranslationProvider;
import top.xfunny.mod.block.LiftTrackMagneticVane;
import top.xfunny.mod.client.InitClient;
import top.xfunny.mod.client.screen.GuiHelper;
import top.xfunny.mod.packet.PacketUpdateLiftTrackMagneticVaneConfig;

public class LiftTrackMagneticVaneScreen extends ScreenExtension implements IGui {

    private static final MutableText TEXT_FLOOR_NUMBER = TranslationProvider.GUI_MTR_LIFT_FLOOR_NUMBER.getMutableText();
    private static final MutableText TEXT_FLOOR_DESCRIPTION = TranslationProvider.GUI_MTR_LIFT_FLOOR_DESCRIPTION.getMutableText();
    private static final int TEXT_FIELD_WIDTH = 240;
    private final TextFieldWidgetExtension textFieldFloorNumber;
    private final TextFieldWidgetExtension textFieldFloorDescription;
    private final BlockPos blockPos;
    private final String initialFloorNumber;
    private final String initialFloorDescription;
    private final int textWidth;

    public LiftTrackMagneticVaneScreen(BlockPos blockPos, LiftTrackMagneticVane.BlockEntity blockEntity) {
        super();
        this.blockPos = blockPos;

        textFieldFloorNumber = new TextFieldWidgetExtension(0, 0, 0, SQUARE_SIZE, 8, TextCase.DEFAULT, null, "1");
        textFieldFloorDescription = new TextFieldWidgetExtension(0, 0, 0, SQUARE_SIZE, 256, TextCase.DEFAULT, null, "Concourse");

        final ClientWorld clientWorld = MinecraftClient.getInstance().getWorldMapped();
        if (clientWorld == null) {
            initialFloorNumber = "EZ";
            initialFloorDescription = "";
        } else {
            initialFloorNumber = blockEntity.getFloorNumber();
            initialFloorDescription = blockEntity.getFloorDescription();
        }

        textWidth = Math.max(GraphicsHolder.getTextWidth(TEXT_FLOOR_NUMBER), GraphicsHolder.getTextWidth(TEXT_FLOOR_DESCRIPTION));
    }

    @Override
    protected void init2() {
        super.init2();
        GuiHelper.clearScreenChildren(this);

        final int startX = (getWidthMapped() - textWidth - TEXT_PADDING - TEXT_FIELD_WIDTH) / 2;
        final int startY = (getHeightMapped() - SQUARE_SIZE * 3 - TEXT_FIELD_PADDING * 2) / 2;
        IDrawing.setPositionAndWidth(textFieldFloorNumber, startX + textWidth + TEXT_PADDING + TEXT_FIELD_PADDING / 2, startY + TEXT_FIELD_PADDING / 2, TEXT_FIELD_WIDTH - TEXT_FIELD_PADDING);
        IDrawing.setPositionAndWidth(textFieldFloorDescription, startX + textWidth + TEXT_PADDING + TEXT_FIELD_PADDING / 2, startY + SQUARE_SIZE + TEXT_FIELD_PADDING * 3 / 2, TEXT_FIELD_WIDTH - TEXT_FIELD_PADDING);

        textFieldFloorNumber.setText2(initialFloorNumber);
        textFieldFloorDescription.setText2(initialFloorDescription);

        addChild(new ClickableWidget(textFieldFloorNumber));
        addChild(new ClickableWidget(textFieldFloorDescription));
    }

    @Override
    public void tick2() {
        textFieldFloorNumber.tick2();
        textFieldFloorDescription.tick2();
    }

    @Override
    public void render(@NotNull GraphicsHolder graphicsHolder, int mouseX, int mouseY, float delta) {
        renderBackground(graphicsHolder);
        final int startX = (getWidthMapped() - textWidth - TEXT_PADDING - TEXT_FIELD_WIDTH) / 2;
        final int startY = (getHeightMapped() - SQUARE_SIZE * 3 - TEXT_FIELD_PADDING * 2) / 2;
        graphicsHolder.drawText(TEXT_FLOOR_NUMBER, startX, startY + TEXT_FIELD_PADDING / 2 + TEXT_PADDING, ARGB_WHITE, false, GraphicsHolder.getDefaultLight());
        graphicsHolder.drawText(TEXT_FLOOR_DESCRIPTION, startX, startY + SQUARE_SIZE + TEXT_FIELD_PADDING * 3 / 2 + TEXT_PADDING, ARGB_WHITE, false, GraphicsHolder.getDefaultLight());
        super.render(graphicsHolder, mouseX, mouseY, delta);
    }

    @Override
    public void onClose2() {
        if (MinecraftClient.getInstance().getWorldMapped() != null) {
            InitClient.REGISTRY_CLIENT.sendPacketToServer(new PacketUpdateLiftTrackMagneticVaneConfig(
                    blockPos, textFieldFloorNumber.getText2(), textFieldFloorDescription.getText2()));
        }
        super.onClose2();
    }

    @Override
    public boolean isPauseScreen2() {
        return false;
    }
}
