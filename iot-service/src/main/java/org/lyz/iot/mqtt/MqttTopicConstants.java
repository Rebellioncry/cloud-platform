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

    public static final String TOPIC_EVENT_PROPERTY_POST = SYS + "/+/" + THING + "/" + EVENT + "/" + PROPERTY + "/" + POST;
    public static final String TOPIC_EVENT_REPORT_POST = SYS + "/+/" + THING + "/" + EVENT + "/" + PLUS + "/" + POST;
    public static final String TOPIC_SERVICE_REPLY = SYS + "/+/" + THING + "/" + SERVICE + "/" + PLUS + "/" + REPLY;

    public static String[] getSubscribeTopics(String group) {
        if (group == null || group.isEmpty()) {
            group = SHARE_GROUP_DEFAULT;
        }
        return new String[]{
                SHARE_PREFIX + "/" + group + "/" + TOPIC_EVENT_PROPERTY_POST,
                SHARE_PREFIX + "/" + group + "/" + TOPIC_EVENT_REPORT_POST,
                SHARE_PREFIX + "/" + group + "/" + TOPIC_SERVICE_REPLY
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
}
