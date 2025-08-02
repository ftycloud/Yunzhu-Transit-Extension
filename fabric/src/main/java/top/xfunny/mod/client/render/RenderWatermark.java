package top.xfunny.mod.client.render;

import org.mtr.mapping.holder.ClientPlayerEntity;
import org.mtr.mapping.holder.MinecraftClient;
import org.mtr.mapping.holder.MutableText;
import org.mtr.mapping.holder.Window;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.TextHelper;
import top.xfunny.mod.Init;
import top.xfunny.mod.Keys;
import top.xfunny.mod.client.InitClient;

import static org.mtr.mod.data.IGui.ARGB_WHITE_TRANSLUCENT;

public class RenderWatermark {
    private static final int a = 10;
    public static void render(GraphicsHolder b){
        final MinecraftClient c = MinecraftClient.getInstance();
        final Window x = c.getWindow();
        int h = x.getScaledWidth() - 1;
        int w = x.getScaledHeight();
        if(!InitClient.getConfig().hideTestWatermark || Init.HAS_UPDATE == 1){
            int j = -1;
            int z = 0;
            String e = "Yunzhu Transit Extension Beta";
            MutableText g = TextHelper.translatable("gui.yte.watermark");
            MutableText q = TextHelper.translatable("gui.yte.watermark_1", Keys.MOD_VERSION, Keys.BUILD_TIME);
            final ClientPlayerEntity p = c.getPlayerMapped();
            if (p != null) {
                b.push();
                b.translate(h, w, z);
                b.drawText(e, -GraphicsHolder.getTextWidth(e), -a * 3, j, true, GraphicsHolder.getDefaultLight());
                b.drawText(g, -GraphicsHolder.getTextWidth(g), -a * 2, j, true, GraphicsHolder.getDefaultLight());
                b.drawText(q, -GraphicsHolder.getTextWidth(q), -a, j, true, GraphicsHolder.getDefaultLight());
                b.pop();
            }
        }
        if (Init.HAS_UPDATE == 1){
            float s = 1.5F;
            MutableText r = TextHelper.literal("YTE 开发人员版本");
            MutableText n = TextHelper.literal("请转到 Modrinth 下载最新版本。");
            b.translate(h - 80 -GraphicsHolder.getTextWidth(n), w - 50, 0);
            b.drawText(n, 0, 0, ARGB_WHITE_TRANSLUCENT, false, GraphicsHolder.getDefaultLight());
            b.scale(s,s,s);
            b.drawText(r, 0, -a, ARGB_WHITE_TRANSLUCENT, false, GraphicsHolder.getDefaultLight());
        }
    }
}
