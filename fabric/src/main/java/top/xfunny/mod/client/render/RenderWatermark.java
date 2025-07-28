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
    public static final String t = n.format(DateTimeFormatter.ofPattern("yyMMdd-HHmmXX"));
    public static void render(GraphicsHolder b) {
        if(!InitClient.getConfig().hideTestWatermark){
            final MinecraftClient c = MinecraftClient.getInstance();
            final Window x = c.getWindow();
            int j = ARGB_WHITE;
            int z = 0;
            String e = "Yunzhu Transit Extension Beta";
            String f = "1.0.2-beta.3";
            MutableText g = TextHelper.translatable("gui.yte.watermark");
            MutableText q = TextHelper.translatable("gui.yte.watermark_1", f, t);
            int h = x.getScaledWidth();
            int w = x.getScaledHeight();
            final ClientPlayerEntity p = c.getPlayerMapped();
            if (p != null) {
                b.push();
                b.translate(h, w, z);
                b.drawText(e, -GraphicsHolder.getTextWidth(e) - 1, -a * 3, j, true, GraphicsHolder.getDefaultLight());
                b.drawText(g, -GraphicsHolder.getTextWidth(g) - 1, -a * 2, j, true, GraphicsHolder.getDefaultLight());
                b.drawText(q, -GraphicsHolder.getTextWidth(q) - 1, -a, j, true, GraphicsHolder.getDefaultLight());
                b.pop();
            }
        }
    }
}
