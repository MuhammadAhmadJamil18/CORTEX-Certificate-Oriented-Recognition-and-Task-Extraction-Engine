package org.qualitydxb.infrastructure;

import java.io.InputStream;
import java.util.Properties;

public class SystemProperties {
    private static final Properties properties = new Properties();

    static {
        try (InputStream inputStream = SystemProperties.class.getClassLoader().getResourceAsStream("configuration.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new RuntimeException("Could not load or find configuration.properties file.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProcessingUrl() {
        return properties.getProperty("processingUrl", "http://127.0.0.1:5000");
    }

    public static String getSecretKey() {
        return properties.getProperty("tokenSecretKey", "sDhp0qEf4e3XZgVZBgXx3bfZbKzN6Qc1JwpMPxObf8Q=y");
    }

    public static String getEncryptionKey() {
        return properties.getProperty("aesEncryptionKey", "wKgdbCE8c1NvqdhPTfBoRg==");
    }

    public static Integer getMaxConcurrentFiles() {return Integer.valueOf(properties.getProperty("maxConcurrentFiles", "5"));}

    public static String getNotifyEmail() {
        return properties.getProperty("notifyEmail", "no-reply@datapulsetechnologies.org");
    }

    public static String getNotifyEmailPassword() {
        return properties.getProperty("notifyEmailPassword", "kijw yiyb wxuj honq");
    }

    public static Integer getNotificationScheduleInterval() {
        return Integer.valueOf(properties.getProperty("notificationScheduleInterval", "60000"));
    }

    public static String getFrontendUrl() {
        return properties.getProperty("frontendUrl", "http://localhost:3000");
    }

}
