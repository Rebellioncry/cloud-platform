package org.lyz.simulator.sim;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.lyz.simulator.config.LiveConfig;
import org.lyz.simulator.model.DeviceInfo;
import org.lyz.simulator.model.ThingModel;
import org.lyz.simulator.mqtt.MqttSimulator;
import org.lyz.simulator.mqtt.TopicUtils;

import java.util.*;

public class PropertySimulator {

    private final MqttSimulator mqtt;
    private final LiveConfig config;
    private final DeviceInfo device;
    private final Random random = new Random();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private long reportCount = 0;

    public PropertySimulator(MqttSimulator mqtt, LiveConfig config, DeviceInfo device) {
        this.mqtt = mqtt;
        this.config = config;
        this.device = device;
    }

    public void report() {
        if (!mqtt.isConnected()) return;
        ThingModel model = device.getThingModel();
        if (model == null || model.getProperties().isEmpty()) return;

        Map<String, Object> params = new LinkedHashMap<>();
        for (ThingModel.PropertyDef prop : model.getProperties()) {
            params.put(prop.identifier, generateValue(prop));
        }

        try {
            String topic = TopicUtils.buildPropertyPostTopic(device.getProductKey(), device.getDeviceName());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", "msg-" + (++reportCount));
            payload.put("method", "thing.property.post");
            payload.put("params", params);
            mqtt.publish(topic, objectMapper.writeValueAsString(payload), 1);
        } catch (Exception e) {
            System.err.println("属性上报失败: " + e.getMessage());
        }
    }

    private Object generateValue(ThingModel.PropertyDef prop) {
        String type = prop.dataType != null ? prop.dataType : "text";
        return switch (type) {
            case "int32", "int" -> {
                int min = (int) prop.min;
                int max = (int) prop.max;
                int step = Math.max((int) prop.step, 1);
                int range = (max - min) / step;
                yield range > 0 ? min + random.nextInt(range + 1) * step : min;
            }
            case "float", "double" -> {
                double val = prop.min + random.nextDouble() * (prop.max - prop.min);
                yield type.equals("float") ? (float) Math.round(val * 10) / 10.0 : Math.round(val * 100) / 100.0;
            }
            case "bool" -> random.nextBoolean();
            case "enum" -> {
                if (prop.enumValues != null && !prop.enumValues.isEmpty()) {
                    List<String> keys = new ArrayList<>(prop.enumValues.keySet());
                    yield keys.get(random.nextInt(keys.size()));
                }
                yield "0";
            }
            default -> "simulated";
        };
    }

    public long getReportCount() { return reportCount; }
}
