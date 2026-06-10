// Developed by alikuxac - Project Vortexia
package me.alikuxac.vortexia.addon.storage.util;

import me.alikuxac.vortexia.addon.storage.StorageAddon;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/alikuxac/Vortexia/releases/latest";
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:-(.+))?");

    private final StorageAddon addon;
    private final String currentVersion;
    private String latestVersion;
    private volatile boolean updateAvailable;
    private volatile boolean checked;

    public UpdateChecker(StorageAddon addon) {
        this.addon = addon;
        this.currentVersion = addon.getPluginMeta().getVersion();
        this.updateAvailable = false;
        this.checked = false;
    }

    public void checkAsync() {
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) URI.create(GITHUB_API_URL).toURL().openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setRequestProperty("User-Agent", "VortexiaStorageAddon-UpdateChecker");

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    addon.getLogger().warning("Storage Addon update check failed with HTTP " + responseCode);
                    checked = true;
                    return;
                }

                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                String tag = extractJsonField(response.toString(), "tag_name");
                if (tag != null && !tag.isEmpty()) {
                    latestVersion = tag.replaceFirst("^v", "");
                    updateAvailable = isOlderVersion(currentVersion, latestVersion);
                    checked = true;

                    if (updateAvailable) {
                        addon.getLogger().warning("A new version of Vortexia-Addon-Storage is available: v" + latestVersion + " (current: v" + currentVersion + ")");
                        addon.getLogger().warning("Download: https://github.com/alikuxac/Vortexia/releases/latest");
                    } else {
                        addon.getLogger().info("Vortexia-Addon-Storage is up to date (v" + currentVersion + ")");
                    }
                } else {
                    checked = true;
                }
            } catch (Exception e) {
                addon.getLogger().warning("Storage Addon update check failed: " + e.getMessage());
                checked = true;
            }
        }, "Vortexia-Storage-UpdateChecker").start();
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public boolean hasChecked() {
        return checked;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    private static String extractJsonField(String json, String field) {
        String search = "\"" + field + "\":";
        int index = json.indexOf(search);
        if (index == -1) return null;
        int start = json.indexOf("\"", index + search.length());
        if (start == -1) return null;
        int end = json.indexOf("\"", start + 1);
        if (end == -1) return null;
        return json.substring(start + 1, end);
    }

    private static boolean isOlderVersion(String current, String latest) {
        int[] currentParts = parseVersion(current);
        int[] latestParts = parseVersion(latest);

        if (currentParts == null || latestParts == null) {
            return !current.equals(latest);
        }

        for (int i = 0; i < 3; i++) {
            if (currentParts[i] < latestParts[i]) return true;
            if (currentParts[i] > latestParts[i]) return false;
        }

        boolean currentIsPre = current.contains("-");
        boolean latestIsPre = latest.contains("-");

        if (currentIsPre && !latestIsPre) return true;
        if (!currentIsPre && !latestIsPre) return false;
        if (currentIsPre && latestIsPre) return current.compareTo(latest) < 0;

        return false;
    }

    private static int[] parseVersion(String version) {
        String clean = version.replaceFirst("^v", "");
        Matcher matcher = VERSION_PATTERN.matcher(clean);
        if (!matcher.matches()) return null;
        return new int[]{
            Integer.parseInt(matcher.group(1)),
            Integer.parseInt(matcher.group(2)),
            Integer.parseInt(matcher.group(3))
        };
    }
}
