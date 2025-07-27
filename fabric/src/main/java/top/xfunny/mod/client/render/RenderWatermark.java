package top.xfunny.mod.client.render;

import org.mtr.mapping.holder.ClientPlayerEntity;
import org.mtr.mapping.holder.MinecraftClient;
import org.mtr.mapping.holder.MutableText;
import org.mtr.mapping.holder.Window;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.TextHelper;
import org.mtr.mod.data.IGui;
import top.xfunny.mod.client.InitClient;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class RenderWatermark implements IGui {



    private static final int a = 10;
    private static final ZonedDateTime n = ZonedDateTime.now();
    private static final String t = n.format(DateTimeFormatter.ofPattern("yyMMdd-HHmmXX"));
    public static void render(GraphicsHolder b) {
        final MinecraftClient c = MinecraftClient.getInstance();
        final Window x = c.getWindow();
        int j = ARGB_WHITE;
        int z = 0;
        float s = 1.5F;
        String e = "\u0059\u0075\u006e\u007a\u0068\u0075\u0020\u0054\u0072\u0061\u006e\u0073\u0069\u0074\u0020\u0045\u0078\u0074\u0065\u006e\u0073\u0069\u006f\u006e\u0020\u0042\u0065\u0074\u0061";
        String f = "1.0.2-beta.3";
        MutableText g = TextHelper.translatable("gui.yte.watermark");
        MutableText q = TextHelper.translatable("gui.yte.watermark_1", f, t);
        MutableText r = TextHelper.translatable("gui.yte.watermark_2");
        MutableText n = TextHelper.translatable("gui.yte.watermark_3");
        int h = x.getScaledWidth();
        int w = x.getScaledHeight();
        final ClientPlayerEntity p = c.getPlayerMapped();
        if (p != null) {
            b.push();
            b.translate(h, w, z);
            b.drawText(e, -GraphicsHolder.getTextWidth(e) - 2, -a * 3, j, true, GraphicsHolder.getDefaultLight());
            b.drawText(g, -GraphicsHolder.getTextWidth(g) - 2, -a * 2, j, true, GraphicsHolder.getDefaultLight());
            b.drawText(q, -GraphicsHolder.getTextWidth(q) - 2, -a, j, true, GraphicsHolder.getDefaultLight());
            b.pop();
            b.translate(h - 82 -GraphicsHolder.getTextWidth(n), w - 50, z);
            b.drawText(n, 0, 0, ARGB_WHITE_TRANSLUCENT, false, GraphicsHolder.getDefaultLight());
            b.scale(s,s,s);
            b.drawText(r, 0, -a, ARGB_WHITE_TRANSLUCENT, false, GraphicsHolder.getDefaultLight());
        if(!InitClient.getConfig().hideTestWatermark){
            final MinecraftClient c = MinecraftClient.getInstance();
            final Window x = c.getWindow();
            int j = ARGB_WHITE_TRANSLUCENT;
            int z = 0;
            float s = 0.9F;
            String e = "\u0059\u0075\u006e\u007a\u0068\u0075\u0020\u0054\u0072\u0061\u006e\u0073\u0069\u0074\u0020\u0045\u0078\u0074\u0065\u006e\u0073\u0069\u006f\u006e\u0020\u0042\u0065\u0074\u0061";
            String f = "1.0.2-beta.3";
            MutableText g = TextHelper.translatable("gui.yte.watermark");
            MutableText q = TextHelper.translatable("gui.yte.watermark_1", f, t);
            int h = x.getScaledWidth();
            int w = x.getScaledHeight();
            final ClientPlayerEntity p = c.getPlayerMapped();
            if (p != null) {
                b.push();
                b.translate(h, w - 25 , z);
                b.scale(s,s,s);
                b.drawText(e, -GraphicsHolder.getTextWidth(e), -a * 3, j, true, GraphicsHolder.getDefaultLight());
                b.drawText(g, -GraphicsHolder.getTextWidth(g), -a * 2, j, true, GraphicsHolder.getDefaultLight());
                b.drawText(q, -GraphicsHolder.getTextWidth(q), -a, j, true, GraphicsHolder.getDefaultLight());
                b.pop();
            }
        }
    }
}
