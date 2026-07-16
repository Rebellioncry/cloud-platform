package org.lyz.iot.mqtt;

public class MqttTopicUtils {

    private static final String SHARE_PREFIX = "$share/";

    public static String extractProductKey(String topic) {
        String[] parts = normalizeTopic(topic).split("/");
        return parts.length > 1 ? parts[1] : "";
    }

    public static String extractDeviceName(String topic) {
        String[] parts = normalizeTopic(topic).split("/");
        return parts.length > 2 ? parts[2] : "";
    }

    public static String extractIdentifier(String topic) {
        String[] parts = normalizeTopic(topic).split("/");
        return parts.length > 4 ? parts[4] : "";
    }

    public static String normalizeTopic(String topic) {
        if (topic == null) return "";
        if (topic.startsWith(SHARE_PREFIX)) {
            int firstSlash = SHARE_PREFIX.length();
            int secondSlash = topic.indexOf('/', firstSlash);
            if (secondSlash > 0) {
                return topic.substring(secondSlash + 1);
            }
        }
        return topic;
    }
}
