package org.lyz.simulator.mqtt;

public class TopicUtils {

    public static final String SYS = "sys";
    public static final String OTA = "ota";
    public static final String DEVICE = "device";
    public static final String THING = "thing";
    public static final String SERVICE = "service";
    public static final String PROPERTY = "property";
    public static final String EVENT = "event";
    public static final String POST = "post";
    public static final String REPLY = "reply";
    public static final String INFORM = "inform";
    public static final String UPGRADE = "upgrade";
    public static final String PROGRESS = "progress";
    public static final String DOWNLOAD = "download";
    public static final String SET = "set";
    public static final String INVOKE = "invoke";

    public static final String SUB_SERVICE_PROPERTY_SET = SYS + "/+/+/" + THING + "/" + SERVICE + "/" + PROPERTY + "/" + SET;
    public static final String SUB_SERVICE_INVOKE = SYS + "/+/+/" + THING + "/" + SERVICE + "/+/" + INVOKE;
    public static final String SUB_OTA_UPGRADE = OTA + "/" + DEVICE + "/" + UPGRADE + "/+/+";

    public static String buildPropertyPostTopic(String productKey, String deviceName) {
        return SYS + "/" + productKey + "/" + deviceName + "/" + THING + "/" + PROPERTY + "/" + POST;
    }

    public static String buildServiceReplyTopic(String productKey, String deviceName, String identifier) {
        return SYS + "/" + productKey + "/" + deviceName + "/" + THING + "/" + SERVICE + "/" + identifier + "/" + REPLY;
    }

    public static String buildOtaInformTopic(String productKey, String deviceName) {
        return OTA + "/" + DEVICE + "/" + INFORM + "/" + productKey + "/" + deviceName;
    }

    public static String buildOtaProgressTopic(String productKey, String deviceName) {
        return OTA + "/" + DEVICE + "/" + PROGRESS + "/" + productKey + "/" + deviceName;
    }

    public static String buildOtaDownloadTopic(String productKey, String deviceName) {
        return OTA + "/" + DEVICE + "/" + DOWNLOAD + "/" + productKey + "/" + deviceName;
    }

    public static String extractProductKey(String topic) {
        String[] parts = topic.split("/");
        return parts.length > 1 ? parts[1] : "";
    }

    public static String extractDeviceName(String topic) {
        String[] parts = topic.split("/");
        return parts.length > 2 ? parts[2] : "";
    }

    public static String extractIdentifier(String topic) {
        String[] parts = topic.split("/");
        return parts.length > 5 ? parts[5] : "";
    }
}
