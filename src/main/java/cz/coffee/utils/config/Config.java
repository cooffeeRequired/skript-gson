package cz.coffee.utils.config;

import cz.coffee.SkJson;
import cz.coffee.utils.ErrorHandler;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static cz.coffee.utils.ErrorHandler.sendMessage;

public class Config {
    private static final File configFile = new File("plugins" + File.separator + SkJson.getInstance().getDescription().getName() + File.separator + "config.yml");

    public static double _CONFIG_VERSION;
    public static boolean _DEBUG = false;
    public static boolean _EXAMPLES;
    public static boolean _REQUEST_HANDLER;
    public static List<Object> _HANDLERS_REQUEST;

    private static DumperOptions dumperOptions() {
        DumperOptions o = new DumperOptions();
        o.setIndent(4);
        o.setPrettyFlow(true);
        o.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        o.setAllowUnicode(true);
        return o;
    }

    public static void init(){
        if (!configFile.exists()) {
            try {
                if (new File(configFile.getParent()).exists()) {
                    configFile.createNewFile();
                    writeDefault();
                } else {
                    new File(configFile.getParent()).mkdirs();
                    configFile.createNewFile();
                    writeDefault();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            load();
        } else {
            load();
        }

    }

    private static void writeDefault() {
        Yaml yml = new Yaml(dumperOptions());
        Map<String,Object> map = null;
        if (configFile.length() > 1) {
            try {
                map = yml.load(new FileInputStream(configFile));
                map.put("version", 2.0);
                map.put("debug", false);
                map.put("create-examples", false);
                map.put("handle-request", true);
                map.put("handlers", List.of("SkriptWebApi","Reqn","reflect"));
            } catch (IOException exception){
                sendMessage(exception.getMessage(), ErrorHandler.Level.ERROR);
            }
        } else {
            map = new HashMap<>();
            map.put("version", 2.0);
            map.put("debug", false);
            map.put("create-examples", false);
            map.put("handle-request", true);
            map.put("handlers", List.of("SkriptWebApi","Reqn","reflect"));
        }
        try (PrintWriter pw = new PrintWriter(configFile)) {
            yml.dump(map, pw);
            pw.flush();
            pw.close();
            writeComments("do not change the 'version'");
        } catch (IOException exception){
            sendMessage(exception.getMessage(), ErrorHandler.Level.ERROR);
        }
    }

    private static void writeComments(String ...comments) {
        try {
            if (comments != null) {
                for (String c : comments) {
                    String comment = "# " + c;
                    Files.write(configFile.toPath(), comment.getBytes(), StandardOpenOption.APPEND);
                }
            }
        } catch (IOException ioException) {
            sendMessage(ioException.getMessage(), ErrorHandler.Level.ERROR);
        }
    }



    public static void load() {
        Yaml yml = new Yaml(dumperOptions());
        FileInputStream fis;
        try {
            fis = new FileInputStream(configFile);
            Map<String, Object> map = yml.load(fis);
            _CONFIG_VERSION = Double.parseDouble(map.get("version").toString());
            _DEBUG = Boolean.parseBoolean(map.get("debug").toString());
            _EXAMPLES = Boolean.parseBoolean(map.get("create-examples").toString());
            _REQUEST_HANDLER = Boolean.parseBoolean(map.get("handle-request").toString());
            _HANDLERS_REQUEST = (ArrayList<Object>) map.get("handlers");
            fis.close();
        } catch (IOException exception) {
            sendMessage(exception.getMessage(), ErrorHandler.Level.ERROR);
        }
    }

}
