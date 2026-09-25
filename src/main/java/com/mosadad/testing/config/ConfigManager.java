package com.mosadad.testing.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads config.properties, then credentials.properties (gitignored) if
 * present. -Dkey=value overrides both.
 */
public final class ConfigManager {

    private static final Logger log = LogManager.getLogger(ConfigManager.class);
    private static final Properties props = new Properties();

    static {
        loadResource("config.properties", true);
        loadResource("credentials.properties", false);
    }

    private static void loadResource(String name, boolean required) {
        try (InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream(name)) {
            if (is == null) {
                if (required) {
                    throw new IllegalStateException(name + " not found on classpath");
                }
                log.warn("{} not found on classpath — copy {}.example if you need real QA credentials.", name, name);
                return;
            }
            props.load(is);
            log.info("{} loaded successfully", name);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load " + name + ": " + e.getMessage());
        }
    }

    private ConfigManager() {}

    public static String get(String key) {
        return System.getProperty(key, props.getProperty(key));
    }

    private static final String DEFAULT_PLATFORM = "qa";

    /** Platform is "qa" or "stage". */
    public static String getBaseUrl(String platform)    { return get("base.url." + platform); }
    public static String getBaseUrl()                   { return getBaseUrl(DEFAULT_PLATFORM); }

    public static String getLoginUrl(String platform)    { return getBaseUrl(platform) + get("login.path"); }
    public static String getLoginUrl()                   { return getLoginUrl(DEFAULT_PLATFORM); }

    public static String getApiGatewayUrl(String platform) { return get("api.gateway.url." + platform); }
    public static String getApiGatewayUrl()                 { return getApiGatewayUrl(DEFAULT_PLATFORM); }

    public static String getBrowser()      { return get("browser"); }
    public static boolean isHeadless()     { return Boolean.parseBoolean(get("headless")); }
    public static int getSlowMoMs()        { return Integer.parseInt(get("slowmo.ms")); }

    public static int getExplicitWaitSeconds()    { return Integer.parseInt(get("explicit.wait.seconds")); }
    public static int getNavigationTimeoutSeconds() { return Integer.parseInt(get("navigation.timeout.seconds")); }

    /** Reads {platform}.claimant.email.{userKey} from credentials.properties. */
    public static String getEmail(String platform, String userKey) {
        String value = get(platform + ".claimant.email." + userKey);
        if (value == null) {
            throw new IllegalArgumentException(
                "No email found for platform='" + platform + "', userKey='" + userKey +
                "' — expected " + platform + ".claimant.email." + userKey + " in credentials.properties");
        }
        return value;
    }

    public static String getPassword(String platform, String userKey) {
        String value = get(platform + ".claimant.password." + userKey);
        if (value == null) {
            throw new IllegalArgumentException(
                "No password found for platform='" + platform + "', userKey='" + userKey +
                "' — expected " + platform + ".claimant.password." + userKey + " in credentials.properties");
        }
        return value;
    }
}
