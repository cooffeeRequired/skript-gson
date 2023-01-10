package cz.coffee.utils.github;

import static cz.coffee.utils.ErrorHandler.Level.INFO;
import static cz.coffee.utils.ErrorHandler.sendMessage;

public class Version {

    private static int serverVersion;
    private final static int STATIC_VERSION = 1165;

    public Version(String version) {
        serverVersion = Integer.parseInt(version.split("-")[0].replaceAll("[.]", ""));
    }
    public Version(Number version) {
        serverVersion = version.intValue();
    }

    public void check() {
        if (serverVersion > STATIC_VERSION) {
            sendMessage(serverVersion, INFO);
        }
    }
    public boolean isLegacy() {
        return serverVersion <= STATIC_VERSION;
    }
    public int getServerVersion() {
        return serverVersion;
    }
    public int getStaticVersion() {
        return STATIC_VERSION;
    }
}
