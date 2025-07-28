package top.xfunny.mod.util;

import com.google.gson.*;
import top.xfunny.mod.Init;
import top.xfunny.mod.Keys;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.*;

public class VersionUpdateCheck {
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:-([a-zA-Z0-9.]+))?");
    private static String REMOTE_VERSION;

    private static String HttpsfetchJson(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setRequestProperty("Accept", "application/json");

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpsURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code : " + responseCode);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    public static int VersionCheck(String urlString) {
        Init.LOGGER.info("Checking mod updates...");
        try {
            String JsonData = HttpsfetchJson(urlString);
            String currentGameVersion = "1.20.1";

            JsonArray versions = JsonParser.parseString(JsonData).getAsJsonArray();

            for (JsonElement element : versions) {
                JsonObject version = element.getAsJsonObject();

                if (matchesEnvironment(version, currentGameVersion)) {
                    REMOTE_VERSION = version.get("version_number").getAsString();
                }
            }

            int result = compareVersions(REMOTE_VERSION);

            if (result == -1){
                Init.LOGGER.info("A new version has been released. Get the latest version here: https://modrinth.com/mod/yunzhu-transit-extension/versions");
            } else if (result == 1) {
                Init.LOGGER.warn("You are using the development version!");
            }

            return result;
        } catch (Exception e){
            Init.LOGGER.error(e);
            return 0;
        }
    }

    private static boolean matchesEnvironment(JsonObject version, String gameVersion){
        JsonArray gameVersions = version.get("game_versions").getAsJsonArray();
        boolean versionMatch = false;

        for (JsonElement v : gameVersions) {
            if (v.getAsString().equals(gameVersion)) {
                versionMatch = true;
                break;
            }
        }

        return versionMatch;
    }

    /**
     * 比较两个语义化版本号 (SemVer)
     * @return -1: v1 < v2 | 0: v1 == v2 | 1: v1 > v2
     */
    private static int compareVersions(String v2) {
        Matcher m1 = VERSION_PATTERN.matcher(Keys.MOD_VERSION);
        Matcher m2 = VERSION_PATTERN.matcher(v2);

        if (!m1.find() || !m2.find()) {
            throw new IllegalArgumentException();
        }

        // 比较主版本号 (MAJOR.MINOR.PATCH)
        for (int i = 1; i <= 3; i++) {
            int num1 = Integer.parseInt(m1.group(i));
            int num2 = Integer.parseInt(m2.group(i));
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }

        // 处理预发布标签 (PRERELEASE)
        String pre1 = m1.group(4);
        String pre2 = m2.group(4);

        // 有预发布标签的版本优先级更低 (1.0.0 > 1.0.0-alpha)
        if (pre1 == null && pre2 != null) return 1;
        if (pre1 != null && pre2 == null) return -1;
        if (pre1 == null) return 0;

        // 拆分预发布标签 (如 "beta.1" -> ["beta", "1"])
        String[] parts1 = pre1.split("\\.");
        String[] parts2 = pre2.split("\\.");

        for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
            if (parts1[i].matches("\\d+") && parts2[i].matches("\\d+")) {
                int num1 = Integer.parseInt(parts1[i]);
                int num2 = Integer.parseInt(parts2[i]);
                if (num1 != num2) {
                    return Integer.compare(num1, num2);
                }
            } else {
                int cmp = parts1[i].compareTo(parts2[i]);
                if (cmp != 0) {
                    return cmp;
                }
            }
        }

        return Integer.compare(parts1.length, parts2.length);
    }
}
