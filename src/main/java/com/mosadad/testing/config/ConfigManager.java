package com.mosadad.testing.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton config loader. Reads src/test/resources/config.properties, and
 * (if present) src/test/resources/credentials.properties on top of it.
 *
 * credentials.properties is gitignored — it holds real QA login secrets
 * locally. In CI, skip the file entirely and inject values with -D instead.
 * Any key can be overridden via a JVM system property (-Dkey=value), which
 * always takes precedence over both files.
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

    /** Returns the value for key, with JVM system property taking precedence. */
    public static String get(String key) {
        return System.getProperty(key, props.getProperty(key));
    }

    private static final String DEFAULT_PLATFORM = "qa";

    /** Platform is "qa" or "stage" — matches the base.url.<platform> / api.base.url.<platform> keys in config.properties. */
    public static String getBaseUrl(String platform)    { return get("base.url." + platform); }
    public static String getBaseUrl()                   { return getBaseUrl(DEFAULT_PLATFORM); }

    public static String getLoginUrl(String platform)    { return getBaseUrl(platform) + get("login.path"); }
    public static String getLoginUrl()                   { return getLoginUrl(DEFAULT_PLATFORM); }

    public static String getApiBaseUrl(String platform)  { return get("api.base.url." + platform); }
    public static String getApiBaseUrl()                 { return getApiBaseUrl(DEFAULT_PLATFORM); }

    /**
     * The shared Azure API Management gateway all six backend microservices
     * sit behind — confirmed live 2026-09-17 from the real Angular app's
     * network traffic. Append "/&lt;service&gt;" (claims, inthub, invoice,
     * quotation, settlement, tenant) for a given service's base URI; see
     * {@code ApiClient.Service}.
     */
    public static String getApiGatewayUrl(String platform) { return get("api.gateway.url." + platform); }
    public static String getApiGatewayUrl()                 { return getApiGatewayUrl(DEFAULT_PLATFORM); }

    public static String getBrowser()      { return get("browser"); }
    public static boolean isHeadless()     { return Boolean.parseBoolean(get("headless")); }
    public static int getSlowMoMs()        { return Integer.parseInt(get("slowmo.ms")); }

    public static int getExplicitWaitSeconds()    { return Integer.parseInt(get("explicit.wait.seconds")); }
    public static int getNavigationTimeoutSeconds() { return Integer.parseInt(get("navigation.timeout.seconds")); }

    public static int getNormalRepairSlaHours()  { return Integer.parseInt(get("sla.normal.repair.hours")); }
    public static int getTotalLossSlaHours()     { return Integer.parseInt(get("sla.total.loss.hours")); }
    public static int getSalvageVatPercent()     { return Integer.parseInt(get("salvage.vat.percent")); }
    public static int getTotalInsurersUae()      { return Integer.parseInt(get("total.insurers.uae")); }

    /**
     * Looks up one insurer's email by platform + userKey, e.g.
     * {@code getEmail("qa", "dubai")} reads {@code qa.claimant.email.dubai}
     * from credentials.properties. Adding user #21 is two new lines in that
     * properties file — never a new Java method. userKey is whatever comes
     * after the platform in the property name (case-sensitive, matches the
     * file exactly).
     */
    public static String getEmail(String platform, String userKey) {
        String value = get(platform + ".claimant.email." + userKey);
        if (value == null) {
            throw new IllegalArgumentException(
                "No email found for platform='" + platform + "', userKey='" + userKey +
                "' — expected " + platform + ".claimant.email." + userKey + " in credentials.properties");
        }
        return value;
    }

    /** Same as {@link #getEmail(String, String)} but for the matching password key. */
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
