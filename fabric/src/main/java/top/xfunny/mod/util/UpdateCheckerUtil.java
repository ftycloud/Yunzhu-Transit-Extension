package top.xfunny.mod.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.xfunny.mod.Init;
import top.xfunny.mod.Keys;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Map;

//TODO： 需要重写版本检查功能
@Deprecated
public class UpdateCheckerUtil {
    private static final Logger LOGGER = LogManager.getLogger("Yunzhu Transit Extension/Update Utility");
    private static final String UPDATE_VERSION = "";

    //    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:-([a-zA-Z0-9.]+))?");
//    private static String REMOTE_VERSION;
//
//    private static String fetchJson(String urlString) throws IOException {
//        URL url = new URL(urlString);
//        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//
//        connection.setRequestMethod("GET");
//        connection.setConnectTimeout(5000);
//        connection.setReadTimeout(5000);
//        connection.setRequestProperty("Accept", "application/json");
//
//        int responseCode = connection.getResponseCode();
//        if (responseCode != HttpsURLConnection.HTTP_OK) {
//            throw new IOException("HTTP error code : " + responseCode);
//        }
//
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
//            StringBuilder response = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                response.append(line);
//            }
//            return response.toString();
//        } finally {
//            connection.disconnect();
//        }
//    }
//
    public static void init() {
        Init.LOGGER.info("[YTE] Yunzhu Transit Extension {} @ MTR {}", Keys.MOD_VERSION, org.mtr.mod.Keys.MOD_VERSION);
        LOGGER.info("Checking mod updates...");
        checkUpdata();
//        CompletableFuture.runAsync(() -> {
//            try {
//                String jsonData = fetchJson(Keys.API_URL);
//                JsonArray versions = JsonParser.parseString(jsonData).getAsJsonArray();
//
//                String version = versions.get(0).getAsJsonObject().get("version_number").getAsString();
//
//                Matcher matcher = VERSION_PATTERN.matcher(version);
//                if (matcher.find()) {
//                    REMOTE_VERSION = matcher.group(0);
//                }
//
//                Semver localSemver = new Semver(Keys.MOD_VERSION, Semver.SemverType.LOOSE);
//                Semver remoteSemver = new Semver(REMOTE_VERSION, Semver.SemverType.LOOSE);
//
//                int result = remoteSemver.compareTo(localSemver);
//                Init.HAS_UPDATE = Integer.compare(0, result);
//
//                if (Init.HAS_UPDATE == -1) {
//
//                } else if (Init.HAS_UPDATE == 1) {
//                    Init.LOGGER.warn("You are using a development version!");
//                }
//            } catch (SemverException e) {
//                Init.LOGGER.error(e.getMessage());
//            } catch (Exception e) {
//                Init.LOGGER.error("Update check failed", e);
//            }
//        });
    }

    private static void checkUpdata(){
        String CurrentVersionHash = getCurrentVersionHash();

        LOGGER.info("New version available: {} → {}", Keys.MOD_VERSION, UPDATE_VERSION);
        LOGGER.info("Get the latest version here: https://modrinth.com/mod/yunzhu-transit-extension/versions");
    }

    private static Map<String, Instant> getUpdateVersions(String Hash){
        return Map.of();
    }

    private static String getCurrentVersionHash(){
        try{
            File jarFile = new File(UpdateCheckerUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            if (!jarFile.getName().toLowerCase().endsWith(".jar") || !jarFile.isFile()){
                LOGGER.info("DEBUG Mode/Skipped");
                Init.HAS_UPDATE = 1;
                return "";
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-1");

            try(InputStream is = new FileInputStream(jarFile)){
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1){
                    digest.update(buffer, 0, bytesRead);
                }

                StringBuilder hexString = new StringBuilder();
                for (byte b : digest.digest()){
                    hexString.append(String.format("%02x",b));
                }

                return hexString.toString();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
