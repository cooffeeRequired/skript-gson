/**
 * Copyright CooffeeRequired, and SkriptLang team and contributors
 */
package cz.coffee.skriptgson;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.yggdrasil.YggdrasilSerializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cz.coffee.skriptgson.adapters.gsonAdapter.BukkitClassAdapt;
import cz.coffee.skriptgson.adapters.gsonAdapter.SkriptClassAdapt;
import cz.coffee.skriptgson.filemanager.GsonExamples;
import cz.coffee.skriptgson.filemanager.StorageConfigurator;
import cz.coffee.skriptgson.github.Version;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Logger;

import static cz.coffee.skriptgson.utils.Utils.color;


@SuppressWarnings({"unused", "FieldCanBeLocal"})

public class SkriptGson extends JavaPlugin {

    public static final HashMap<String, Object> JSON_HASHMAP = new HashMap<>();
    public static final HashMap<String, File> FILE_JSON_HASHMAP = new HashMap<>();
    public static final Gson gsonAdapter = new GsonBuilder()
            .setPrettyPrinting()
            .enableComplexMapKeySerialization()
            .disableHtmlEscaping()
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new BukkitClassAdapt())
            .registerTypeHierarchyAdapter(YggdrasilSerializable.YggdrasilExtendedSerializable.class, new SkriptClassAdapt())
            .registerTypeHierarchyAdapter(YggdrasilSerializable.class, new SkriptClassAdapt())
            .create();

    private static Logger logger;
    private static PluginManager pm;
    private static Metrics metrics;

    private static SkriptGson instance;
    private static PluginDescriptionFile pdf;
    private SkriptAddon addon;

    public static SkriptGson getInstance() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    public static @NotNull Logger logger() {
        if (logger == null) {
            throw new RuntimeException("The logger is null or empty");
        }
        return logger;
    }

    public static void info(String string) {
        logger.info(color(string));
    }

    public static void warning(String string) {
        logger.warning(color("&e" + string));
    }

    public static void severe(String string) {
        logger.severe(color("&c" + string));
    }

    public static void debug(Object str) {
        logger.severe(color("DEBUG! " + "&r" + str));
    }

    // Simple loggers
    public static void bukkitOut(String string) {
        Bukkit.getServer().getConsoleSender().sendMessage((color("[&askript-gson&r] " + string)));
    }

    // Plugins preload checks
    private boolean canLoadPlugin() {
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
        logger = getLogger();
        return canLoad;
    }

    private void loadMetrics() {
        metrics = new Metrics(this, 16953);
        metrics.addCustomChart(new Metrics.SimplePie("skript_version", () -> Skript.getVersion().toString()));
        bukkitOut("&fMetrics&r: Loaded metrics&a successfully!");
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

        if (!canLoadPlugin()) {
            pm.disablePlugin(this);
            return;
        }
        instance = this;
        addon = Skript.registerAddon(this);
        try {
            addon.loadClasses("cz.coffee.skriptgson.skript");
        } catch (Exception ex) {
            severe("Unable to register " + getDescription().getName() + " syntaxes:\n- " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        // data folder
        new StorageConfigurator().create();
        new GsonExamples();

        // gitHub
        Version.check();

        // metrics
        loadMetrics();
        info("&aFinished loading.");
    }

}
