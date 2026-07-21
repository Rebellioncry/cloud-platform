package org.lyz.iot.mqtt;

public class MqttTopicConstants {

    private static final String SYS = "sys";
    private static final String PLUS = "+";
    private static final String THING = "thing";
    private static final String EVENT = "event";
    private static final String PROPERTY = "property";
    private static final String POST = "post";
    private static final String SERVICE = "service";
    private static final String REPLY = "reply";
    private static final String SHARE_PREFIX = "$share";

    public static final String SHARE_GROUP_DEFAULT = "iot-service";

    public static final String TOPIC_EVENT_PROPERTY_POST = SYS + "/+/+/" + THING + "/" + EVENT + "/" + PROPERTY + "/" + POST;
    public static final String TOPIC_PROPERTY_POST = SYS + "/+/+/" + THING + "/" + PROPERTY + "/" + POST;
    public static final String TOPIC_EVENT_PROPERTY_PACK_POST = SYS + "/+/+/" + THING + "/" + EVENT + "/" + PROPERTY + "/pack/" + POST;
    public static final String TOPIC_PROPERTY_PACK_POST = SYS + "/+/+/" + THING + "/" + PROPERTY + "/pack/" + POST;
    public static final String TOPIC_EVENT_PROPERTY_HISTORY_POST = SYS + "/+/+/" + THING + "/" + EVENT + "/" + PROPERTY + "/history/" + POST;
    public static final String TOPIC_PROPERTY_HISTORY_POST = SYS + "/+/+/" + THING + "/" + PROPERTY + "/history/" + POST;
    public static final String TOPIC_EVENT_PROPERTY_BATCH_POST = SYS + "/+/+/" + THING + "/" + EVENT + "/" + PROPERTY + "/batch/" + POST;
    public static final String TOPIC_PROPERTY_BATCH_POST = SYS + "/+/+/" + THING + "/" + PROPERTY + "/batch/" + POST;
    public static final String TOPIC_EVENT_REPORT_POST = SYS + "/+/+/" + THING + "/" + EVENT + "/" + PLUS + "/" + POST;
    public static final String TOPIC_SERVICE_REPLY = SYS + "/+/+/" + THING + "/" + SERVICE + "/" + PLUS + "/" + REPLY;

    private static final String OTA = "ota";
    private static final String DEVICE = "device";
    private static final String INFORM = "inform";
    private static final String UPGRADE = "upgrade";
    private static final String PROGRESS = "progress";
    private static final String DOWNLOAD = "download";

    public static final String TOPIC_OTA_DEVICE_INFORM = OTA + "/device/inform/+/+";
    public static final String TOPIC_OTA_DEVICE_PROGRESS = OTA + "/device/progress/+/+";
    public static final String TOPIC_OTA_DEVICE_DOWNLOAD = OTA + "/device/download/+/+";

    public static String[] getSubscribeTopics(String group) {
        if (group == null || group.isEmpty()) {
            group = SHARE_GROUP_DEFAULT;
        }
        return new String[]{
                SHARE_PREFIX + "/" + group + "/" + TOPIC_EVENT_PROPERTY_POST,
                SHARE_PREFIX + "/" + group + "/" + TOPIC_PROPERTY_POST,
                SHARE_PREFIX + "/" + group + "/" + TOPIC_EVENT_PROPERTY_PACK_POST,
                SHARE_PREFIX + "/" + group + "/" + TOPIC_PROPERTY_PACK_POST,
                SHARE_PREFIX + "/" + group + "/" + TOPIC_EVENT_PROPERTY_HISTORY_POST,
                SHARE_PREFIX + "/" + group + "/" + TOPIC_PROPERTY_HISTORY_POST,
                SHARE_PREFIX + "/" + group + "/" + TOPIC_EVENT_PROPERTY_BATCH_POST,
                SHARE_PREFIX + "/" + group + "/" + TOPIC_PROPERTY_BATCH_POST,
                SHARE_PREFIX + "/" + group + "/" + TOPIC_SERVICE_REPLY,
                SHARE_PREFIX + "/" + group + "/" + TOPIC_OTA_DEVICE_INFORM,
                SHARE_PREFIX + "/" + group + "/" + TOPIC_OTA_DEVICE_PROGRESS,
                SHARE_PREFIX + "/" + group + "/" + TOPIC_OTA_DEVICE_DOWNLOAD
        };
    }

    public static String buildServicePropertySetTopic(String productKey, String deviceName) {
        return SYS + "/" + productKey + "/" + deviceName + "/" + THING + "/" + SERVICE + "/" + PROPERTY + "/set";
    }

    public static String buildServiceInvokeTopic(String productKey, String deviceName, String identifier) {
        return SYS + "/" + productKey + "/" + deviceName + "/" + THING + "/" + SERVICE + "/" + identifier + "/invoke";
    }

    public static String buildEventPropertyPostTopic(String productKey, String deviceName) {
        return SYS + "/" + productKey + "/" + deviceName + "/" + THING + "/" + EVENT + "/" + PROPERTY + "/" + POST;
    }

    public static String buildEventPostTopic(String productKey, String deviceName, String identifier) {
        return SYS + "/" + productKey + "/" + deviceName + "/" + THING + "/" + EVENT + "/" + identifier + "/" + POST;
    }

    public static String buildEventPropertyHistoryPostTopic(String productKey, String deviceName) {
        return SYS + "/" + productKey + "/" + deviceName + "/" + THING + "/" + EVENT + "/" + PROPERTY + "/history/" + POST;
    }

    public static String buildEventPropertyBatchPostTopic(String productKey, String deviceName) {
        return SYS + "/" + productKey + "/" + deviceName + "/" + THING + "/" + EVENT + "/" + PROPERTY + "/batch/" + POST;
    }

    public static String buildEventPropertyPackPostTopic(String productKey, String deviceName) {
        return SYS + "/" + productKey + "/" + deviceName + "/" + THING + "/" + EVENT + "/" + PROPERTY + "/pack/" + POST;
    }

    public static String buildOtaUpgradeTopic(String productKey, String deviceName) {
        return OTA + "/" + DEVICE + "/" + UPGRADE + "/" + productKey + "/" + deviceName;
    }

    public static String buildOtaDeviceInformTopic(String productKey, String deviceName) {
        return OTA + "/" + DEVICE + "/" + INFORM + "/" + productKey + "/" + deviceName;
    }

    public static String buildOtaDeviceProgressTopic(String productKey, String deviceName) {
        return OTA + "/" + DEVICE + "/" + PROGRESS + "/" + productKey + "/" + deviceName;
    }

    public static String buildOtaDeviceDownloadTopic(String productKey, String deviceName) {
        return OTA + "/" + DEVICE + "/" + DOWNLOAD + "/" + productKey + "/" + deviceName;
    }
}
