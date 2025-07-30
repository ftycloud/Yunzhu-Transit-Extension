package top.xfunny.mod.client.screen.widget;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.mapper.ResourceManagerHelper;
import top.xfunny.mod.Init;

import java.io.File;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class CustomSignsManager {
    // 存储所有标志牌资源的列表
    private static final ObjectArrayList<String> allSigns = new ObjectArrayList<>();

    private static final Set<String> builtinFileNames = new HashSet<>(Set.of(""));
    // 内置非图标列表
    private static final Set<String> NAFileNames = Set.of(
            "seven_segment.png","qr_code.png","lift_arrow.png",
            "exit_letter_blank.png","circle.png","door_not_in_use.png",
            "gap_small.png","logo_grayscale.png","rubbish.png","spit.png");

    public static void loader() {
        ObjectArrayList<String> defaultSigns = new ObjectArrayList<>();

        builtinFileNames.clear();
        allSigns.clear();

        ResourceManagerHelper.readAllResources(new Identifier("mtr", "mtr_custom_resources.json"), (inputStream) -> {
            try (InputStreamReader reader = new InputStreamReader(inputStream)) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray signsArray = jsonObject.getAsJsonArray("signs");

                for (JsonElement signElement : signsArray) {
                    JsonObject signObject = signElement.getAsJsonObject();
                    String id = signObject.get("id").getAsString();
                    String textureResource = signObject.get("textureResource").getAsString();

                    String fileName = new File(textureResource).getName();

                    if (id.startsWith("!")) {
                        builtinFileNames.add(fileName);
                    }
                }
            } catch (Exception e) {
                Init.LOGGER.error("Fake MTR", e);
            }
        });

        // 遍历 textures/block/sign 目录，判断是否为内置文件
        ResourceManagerHelper.readDirectory("textures/block/sign", (identifier, inputStream) -> {
            try{
                if (identifier.getNamespace().equals("mtr") && identifier.getPath().endsWith(".png")) {
                    String fileName = new File(identifier.getPath()).getName();
                    String substring = fileName.substring(0, fileName.length() - 4);

                    if (builtinFileNames.contains(fileName)) {
                        defaultSigns.add(substring);
                    } else if (!NAFileNames.contains(fileName)) {
                        allSigns.add(substring);
                    }
                }
            }catch (Exception e){Init.LOGGER.error("Fake MTR", e);}
        });

        allSigns.addAll(0, defaultSigns);
        Init.LOGGER.info("Found {} Icon Files", allSigns.size());
    }

    public static ObjectArrayList<String> getSignList(){
        return allSigns;
    }
}