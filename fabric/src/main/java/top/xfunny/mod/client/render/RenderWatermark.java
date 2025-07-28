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

public class RenderWatermark {
    private static final int a = 10;
    public static void render(GraphicsHolder b){
        if(!InitClient.getConfig().hideTestWatermark || Init.HAS_UPDATE == 1){
            final MinecraftClient c = MinecraftClient.getInstance();
            final Window x = c.getWindow();
            int j = -1;
            int z = 0;
            String e = "Yunzhu Transit Extension Beta";
            MutableText g = TextHelper.translatable("gui.yte.watermark");
            MutableText q = TextHelper.translatable("gui.yte.watermark_1", Keys.MOD_VERSION, Keys.BUILD_TIME);
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
