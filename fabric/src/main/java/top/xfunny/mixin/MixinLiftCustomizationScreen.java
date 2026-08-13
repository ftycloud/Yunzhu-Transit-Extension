package top.xfunny.mixin;

import org.mtr.core.data.Lift;
import org.mtr.mapping.holder.ClickableWidget;
import org.mtr.mapping.holder.Text;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.TextHelper;
import org.mtr.mapping.mapper.TextFieldWidgetExtension;
import org.mtr.mapping.tool.TextCase;
import org.mtr.mod.data.IGui;
import org.mtr.mod.screen.LiftCustomizationScreen;
import org.mtr.mod.screen.MTRScreenBase;
import org.mtr.mod.screen.WidgetShorterSlider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.xfunny.core.data.YteLiftConfig;
import top.xfunny.core.operation.YteUpdateDataRequest;
import top.xfunny.mod.client.InitClient;
import top.xfunny.mod.client.YteMinecraftClientData;
import top.xfunny.mod.config.YteLiftConfigStore;
import top.xfunny.mod.packet.YtePacketUpdateData;

@Mixin(value = LiftCustomizationScreen.class, remap = false)
public abstract class MixinLiftCustomizationScreen extends MTRScreenBase {

    @Shadow
    @Final
    private Lift lift;

    @Shadow
    private int width2;

    @Unique
    private WidgetShorterSlider yte$sliderSpeed;

    @Unique
    private WidgetShorterSlider yte$sliderAcceleration;

    @Unique private TextFieldWidgetExtension yte$adoDistanceField;
    @Unique private TextFieldWidgetExtension yte$levellingDistanceField;
    @Unique private TextFieldWidgetExtension yte$levellingSpeedField;

    @Unique
    private static final int SPEED_SLIDER_MAX = 40;

    @Unique
    private static final int ACCEL_SLIDER_MAX = 20;

    @Unique
    private double yte$lastSentSpeed = -1;

    @Unique
    private double yte$lastSentAccel = -1;
    @Unique private double yte$lastSentAdoDistance = -1;
    @Unique private double yte$lastSentLevellingDistance = -1;
    @Unique private double yte$lastSentLevellingSpeed = -1;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstructed(Lift liftParam, CallbackInfo ci) {
        final long liftId = liftParam.getId();
        final YteLiftConfig config = YteMinecraftClientData.getInstance().getConfig(liftId);

        final double currentSpeed = config != null ? config.getSpeed() : YteLiftConfig.DEFAULT_SPEED;
        final double currentAccel = config != null ? config.getAcceleration() : YteLiftConfig.DEFAULT_ACCELERATION;
        final double currentAdoDistance = config != null ? config.getAdoDistance() : YteLiftConfig.DEFAULT_ADO_DISTANCE;
        final double currentLevellingDistance = config != null ? config.getLevellingDistance() : YteLiftConfig.DEFAULT_LEVELLING_DISTANCE;
        final double currentLevellingSpeed = config != null ? config.getLevellingSpeed() : YteLiftConfig.DEFAULT_LEVELLING_SPEED;

        // 不显示内置值文字，由 render 手绘
        yte$sliderSpeed = new WidgetShorterSlider(0, 60, SPEED_SLIDER_MAX,
                value -> "", null);
        yte$sliderSpeed.setValue(speedToValue(currentSpeed));

        yte$sliderAcceleration = new WidgetShorterSlider(0, 60, ACCEL_SLIDER_MAX,
                value -> "", null);
        yte$sliderAcceleration.setValue(accelToValue(currentAccel));

        yte$adoDistanceField = yte$createNumberField(currentAdoDistance);
        yte$levellingDistanceField = yte$createNumberField(currentLevellingDistance);
        yte$levellingSpeedField = yte$createNumberField(currentLevellingSpeed);

        yte$lastSentSpeed = currentSpeed;
        yte$lastSentAccel = currentAccel;
        yte$lastSentAdoDistance = currentAdoDistance;
        yte$lastSentLevellingDistance = currentLevellingDistance;
        yte$lastSentLevellingSpeed = currentLevellingSpeed;
    }

    @Inject(method = "init2", at = @At("TAIL"))
    private void onInit2(CallbackInfo ci) {
        // 与原版全宽控件对齐：x=0, width=width2
        // Speed: row 11 文字, row 12 滑块
        // Accel: row 13 文字, row 14 滑块
        final int sliderY1 = IGui.SQUARE_SIZE * 12;
        final int sliderY2 = IGui.SQUARE_SIZE * 14;

        yte$sliderSpeed.setX2(0);
        yte$sliderSpeed.setY2(sliderY1);
        yte$sliderSpeed.setHeight(IGui.SQUARE_SIZE);
        yte$sliderSpeed.setWidth2(width2);

        yte$sliderAcceleration.setX2(0);
        yte$sliderAcceleration.setY2(sliderY2);
        yte$sliderAcceleration.setHeight(IGui.SQUARE_SIZE);
        yte$sliderAcceleration.setWidth2(width2);

        addChild(new ClickableWidget(yte$sliderSpeed));
        addChild(new ClickableWidget(yte$sliderAcceleration));

        yte$positionField(yte$adoDistanceField, 15);
        yte$positionField(yte$levellingDistanceField, 16);
        yte$positionField(yte$levellingSpeedField, 17);
        addChild(new ClickableWidget(yte$adoDistanceField));
        addChild(new ClickableWidget(yte$levellingDistanceField));
        addChild(new ClickableWidget(yte$levellingSpeedField));
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(GraphicsHolder graphicsHolder, int mouseX, int mouseY, float delta,
            CallbackInfo ci) {
        // "Speed: X.X m/s" 文字行（滑块上方），左对齐
        final int labelY1 = IGui.SQUARE_SIZE * 11 + IGui.TEXT_PADDING;
        final int labelY2 = IGui.SQUARE_SIZE * 13 + IGui.TEXT_PADDING;

        final double speed = valueToSpeed(yte$sliderSpeed.getIntValue());
        final double accel = valueToAccel(yte$sliderAcceleration.getIntValue());
        final double adoDistance = yte$parseNumber(yte$adoDistanceField, yte$lastSentAdoDistance, YteLiftConfig.MAX_ADO_DISTANCE);
        final double levellingDistance = yte$parseNumber(yte$levellingDistanceField, yte$lastSentLevellingDistance, YteLiftConfig.MAX_LEVELLING_DISTANCE);
        final double levellingSpeed = yte$parseNumber(yte$levellingSpeedField, yte$lastSentLevellingSpeed, YteLiftConfig.MAX_LEVELLING_SPEED);

        graphicsHolder.drawText(
                TextHelper.translatable("gui.yte.lift_speed_value", speed),
                0, labelY1, IGui.ARGB_WHITE, false, GraphicsHolder.getDefaultLight());
        graphicsHolder.drawText(
                TextHelper.translatable("gui.yte.lift_acceleration_value", accel),
                0, labelY2, IGui.ARGB_WHITE, false, GraphicsHolder.getDefaultLight());
        yte$drawInputLabel(graphicsHolder, "gui.yte.lift_ado_distance", 15);
        yte$drawInputLabel(graphicsHolder, "gui.yte.lift_levelling_distance", 16);
        yte$drawInputLabel(graphicsHolder, "gui.yte.lift_levelling_speed", 17);

        if (speed != yte$lastSentSpeed || accel != yte$lastSentAccel
                || adoDistance != yte$lastSentAdoDistance || levellingDistance != yte$lastSentLevellingDistance
                || levellingSpeed != yte$lastSentLevellingSpeed) {
            yte$lastSentSpeed = speed;
            yte$lastSentAccel = accel;
            yte$lastSentAdoDistance = adoDistance;
            yte$lastSentLevellingDistance = levellingDistance;
            yte$lastSentLevellingSpeed = levellingSpeed;

            final long liftId = lift.getId();
            final YteLiftConfig config = new YteLiftConfig(liftId, speed, accel, adoDistance, levellingDistance, levellingSpeed);
            YteLiftConfigStore.put(liftId, speed, accel, adoDistance, levellingDistance, levellingSpeed);

            final YteUpdateDataRequest request = new YteUpdateDataRequest(
                    config, YteMinecraftClientData.getInstance());
            InitClient.REGISTRY_CLIENT.sendPacketToServer(
                    new YtePacketUpdateData(request));
        }
    }

    @Unique
    private static TextFieldWidgetExtension yte$createNumberField(double value) {
        final TextFieldWidgetExtension field = new TextFieldWidgetExtension(0, 0, 0, IGui.SQUARE_SIZE, 12, TextCase.DEFAULT, null, "0");
        field.setText2(Double.toString(value));
        return field;
    }

    @Unique
    private void yte$positionField(TextFieldWidgetExtension field, int row) {
        field.setX2(width2 / 2);
        field.setY2(IGui.SQUARE_SIZE * row);
        field.setWidth2(width2 / 2);
    }

    @Unique
    private void yte$drawInputLabel(GraphicsHolder graphicsHolder, String key, int row) {
        graphicsHolder.drawText(TextHelper.translatable(key), 0, IGui.SQUARE_SIZE * row + IGui.TEXT_PADDING,
                IGui.ARGB_WHITE, false, GraphicsHolder.getDefaultLight());
    }

    @Unique
    private static double yte$parseNumber(TextFieldWidgetExtension field, double fallback, double maximum) {
        try {
            final double value = Double.parseDouble(field.getText2().trim().replace(',', '.'));
            return Double.isFinite(value) ? Math.max(0, Math.min(maximum, value)) : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    @Unique
    private static double valueToSpeed(int sliderValue) {
        return Math.max(0.5, sliderValue * 0.5);
    }

    @Unique
    private static int speedToValue(double speed) {
        return (int) Math.round(speed / 0.5);
    }

    @Unique
    private static double valueToAccel(int sliderValue) {
        return Math.max(0.5, sliderValue * 0.5);
    }

    @Unique
    private static int accelToValue(double accel) {
        return (int) Math.round(accel / 0.5);
    }
}
