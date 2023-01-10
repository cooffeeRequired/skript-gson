/**
 *   This file is part of skJson.
 * <p>
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * <p>
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * <p>
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright coffeeRequired nd contributors
 */

package cz.coffee;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import com.google.gson.JsonElement;
import cz.coffee.utils.SimpleUtil;
import cz.coffee.utils.config.Config;
import cz.coffee.utils.github.Version;
import cz.coffee.utils.github.Updater;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import static cz.coffee.utils.SimpleUtil.hex;

public final class SkJson extends JavaPlugin {

    public static final HashMap<String, JsonElement> JSON_STORAGE = new HashMap<>();
    public static final HashMap<String, File> FILE_JSON_MAP = new HashMap<>();

    private static Logger logger;
    private static final SimpleUtil util = new SimpleUtil();
    private static PluginManager pm;
    private static SkJson instance;
    private static PluginDescriptionFile pdf;

    private static final Version version = new Version(Bukkit.getBukkitVersion());

    public static SkJson getInstance() {
        if (instance == null) {
            throw new IllegalStateException("instance cannot be a null");
        }
        return instance;
    }

    public static @NotNull Logger logger() {
        if (logger == null) {
            throw new RuntimeException("The logger is null or empty");
        }
        return logger;
    }

    private boolean canLoad() {
        logger = getLogger();
        boolean canLoad = true;
        String reason = null;
        Plugin skriptPlugin = pm.getPlugin("Skript");
        if (skriptPlugin == null) {
            reason = "Plugin 'Skript' is not found!";
            canLoad = false;
        } else if (!skriptPlugin.isEnabled()) {
            reason = "Plugin 'Skript' is not enabled!";
            canLoad = false;
        }
        if (!canLoad) {
            severe("Could not load " + pdf.getName() + ":\n- " + reason);
        }
        return canLoad;
    }


    private void loadMetrics() {
        Metrics metrics = new Metrics(this, 17374);
        metrics.addCustomChart(new Metrics.SimplePie("skript_version", () -> Skript.getVersion().toString()));
        console("&fMetrics&r: Loaded metrics&a successfully!");
    }

    public PluginManager getPluginManager() {
        return this.getServer().getPluginManager();
    }

    @Override
    public void onDisable() {
        info("&eDisabling... good bye!");
    }



    @Override
    public void onEnable() {
        pm = getPluginManager();
        pdf = this.getDescription();
        if (!canLoad()) {
            pm.disablePlugin(this);
            return;
        }
        instance = this;
        SkriptAddon addon = Skript.registerAddon(this);
        try {
            addon.loadClasses("cz.coffee.skript");
        } catch (Exception ex) {
            severe("Unable to register " + getDescription().getName() + " syntaxes:\n- " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        //metrics
        loadMetrics();

        // github version checker
        new Updater(version);

        // Config folder
        Config.init();
        console("&aFinished loading.");
    }
    // Logging
    public static void info(String string) {
        logger.info(util.color(string));
    }

    public static void warning(String string) {
        logger.warning(util.color("&e" + string));
    }

    public static void severe(String string) {
        logger.severe(util.color("&c" + string));
    }

    public static void debug(Object str) {
        logger.severe(util.color("DEBUG! " + "&r" + str));
    }

    // Simple loggers
    public static void console(String string) {
        String prefix =  version.isLegacy() ? util.color("&7[&ask&2Json&7]") : "&7["+hex("#B6E69Cs#9BD97Ek#80CC61J#65BF43s#4AB226o#2FA508n")+"&7]";
        Bukkit.getServer().getConsoleSender().sendMessage(util.color(prefix + " " + (version.isLegacy() ? util.color(string) : hex(string))));
    }
}
