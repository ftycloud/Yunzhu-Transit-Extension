package top.xfunny.mod.util;

import com.google.gson.*;
import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;
import top.xfunny.mod.Init;
import top.xfunny.mod.Keys;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.regex.*;

public class VersionUpdateCheck {
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:-([a-zA-Z0-9.]+))?");
    private static String REMOTE_VERSION;

    private static String fetchJson(String urlString) throws IOException {
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

    public static void init() {
        Init.LOGGER.info("Checking mod updates...");
        CompletableFuture.runAsync(() -> {
            try {
                String jsonData = fetchJson(Keys.API_URL);
                JsonArray versions = JsonParser.parseString(jsonData).getAsJsonArray();

                String version = versions.get(0).getAsJsonObject().get("version_number").getAsString();

                Matcher matcher = VERSION_PATTERN.matcher(version);
                if (matcher.find()) {
                    REMOTE_VERSION = matcher.group(0);
                }

                Semver localSemver = new Semver(Keys.MOD_VERSION, Semver.SemverType.LOOSE);
                Semver remoteSemver = new Semver(REMOTE_VERSION, Semver.SemverType.LOOSE);

                int result = remoteSemver.compareTo(localSemver);
                Init.HAS_UPDATE = Integer.compare(0, result);

                if (Init.HAS_UPDATE == -1) {
                    Init.LOGGER.warn("New version available: {} → {}", Keys.MOD_VERSION, REMOTE_VERSION);
                    Init.LOGGER.warn("Get the latest version here: https://modrinth.com/mod/yunzhu-transit-extension/versions");
                } else if (Init.HAS_UPDATE == 1) {
                    Init.LOGGER.warn("You are using a development version!");
                }
            } catch (SemverException e) {
                Init.LOGGER.error(e.getMessage());
            } catch (Exception e) {
                Init.LOGGER.error("Update check failed", e);
            }
        });
    }
}
