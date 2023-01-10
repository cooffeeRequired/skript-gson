package cz.coffee.utils.github;

import com.google.gson.JsonElement;
import cz.coffee.SkJson;
import cz.coffee.utils.ErrorHandler;
import cz.coffee.utils.HTTPHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static cz.coffee.utils.ErrorHandler.sendMessage;

public class Updater {
    private static final PluginDescriptionFile pdf;
    static {
        pdf = SkJson.getInstance().getDescription();
    }
    private static final String latestLink =  "https://api.github.com/repos/cooffeeRequired/"+pdf.getName()+"/releases/latest";
    private static final String userVer = pdf.getVersion();
    private static String latestVersion;
    private static String status;
    private int responseCode;
    public Updater(Version version) {
        SkJson.console("&7Checking for updates..");
        if (SkJson.getInstance().getDescription().getVersion().endsWith("-B")){
            status = "BETA";
            SkJson.console("You're running on beta version, so checking is not necessary &bv" + userVer + "&r!");
        } else {
            if (version.isLegacy()) {
                SkJson.console("&eYou're running on Legacy minecraft version &6 " + Bukkit.getServer().getVersion());
            }
            init(version);
            if (responseCode != 200) {
                SkJson.console("Do you have internet connection? Version check &cfailed");
                return;
            } else {
                if (getStatus().equals("OUTDATED")) {
                    SkJson.console("&cskJson is not up to date!");
                    SkJson.console("&8 > &7Current version: &cv" + userVer);
                    SkJson.console("&8 > &7Available version: &av" + latestVersion);
                    SkJson.console("&8 > &7Download available at link: &bhttps://github.com/cooffeeRequired/skript-gson/releases");
                } else {
                    SkJson.console("You're running on &alatest stable &fversion!");
                }
            }
        }
    }


    public static String getStatus() {
        return status;
    }

    public static URL getUpdateURL() {
        try {
            return new URL(latestLink);
        } catch (MalformedURLException e) {
            sendMessage(e.getMessage(), ErrorHandler.Level.INFO);
        }
        return null;
    }

    private void init(Version v){
        File file = new File(SkJson.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        HTTPHandler http = null;
        String link2LatestVersion = null;
        http = new HTTPHandler(latestLink, "GET");

        http.setProperty("Accept", "application/vnd.github+json");
        http.setTimeout(1000);
        http.connect();
        responseCode = http.getResponse();
        if (responseCode != 200) {
            return;
        }
        JsonElement contents = (JsonElement) http.getContents(true);
        if (v.isLegacy()) {
            link2LatestVersion = contents.getAsJsonObject().getAsJsonArray("assets").get(1).getAsJsonObject().get("browser_download_url").getAsString();
        }
        link2LatestVersion = contents.getAsJsonObject().getAsJsonArray("assets").get(0).getAsJsonObject().get("browser_download_url").getAsString();
        latestVersion = contents.getAsJsonObject().get("tag_name").getAsString();
        Hash currentJarHash = new Hash(file, "SHA-256");
        Hash gitLatestJarHash = null;
        try {
            gitLatestJarHash = new Hash(new URL(link2LatestVersion), "SHA-256");
        } catch (MalformedURLException mlw) {
            sendMessage(mlw.getMessage(), ErrorHandler.Level.ERROR);
        }
        assert gitLatestJarHash != null;
        if (!Objects.equals(gitLatestJarHash.get(), currentJarHash.get())) {
            status = "OUTDATED";
        } else {
            status = "LATEST";
        }
    }
}
