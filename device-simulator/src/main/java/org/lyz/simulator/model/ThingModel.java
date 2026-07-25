package org.lyz.simulator.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class ThingModel {

    private List<PropertyDef> properties = new ArrayList<>();
    private List<ServiceDef> services = new ArrayList<>();
    private List<EventDef> events = new ArrayList<>();

    public static ThingModel parse(String json) {
        ThingModel model = new ThingModel();
        if (json == null || json.isEmpty()) return model;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            JsonNode props = root.get("properties");
            if (props != null && props.isArray()) {
                for (JsonNode p : props) {
                    PropertyDef def = new PropertyDef();
                    def.identifier = p.has("identifier") ? p.get("identifier").asText() : "";
                    def.name = p.has("name") ? p.get("name").asText() : "";
                    def.accessMode = p.has("accessMode") ? p.get("accessMode").asText() : "rw";
                    JsonNode dt = p.get("dataType");
                    if (dt != null) {
                        def.dataType = dt.has("type") ? dt.get("type").asText() : "text";
                        JsonNode specs = dt.get("specs");
                        if (specs != null) {
                            def.min = specs.has("min") ? specs.get("min").asDouble() : 0;
                            def.max = specs.has("max") ? specs.get("max").asDouble() : 100;
                            def.step = specs.has("step") ? specs.get("step").asDouble() : 1;
                            def.unit = specs.has("unit") ? specs.get("unit").asText() : "";
                            if (specs.has("enumValues")) {
                                def.enumValues = new LinkedHashMap<>();
                                specs.get("enumValues").fields().forEachRemaining(e ->
                                        def.enumValues.put(e.getKey(), e.getValue().asText()));
                            }
                        }
                    }
                    model.properties.add(def);
                }
            }

            JsonNode svcs = root.get("services");
            if (svcs != null && svcs.isArray()) {
                for (JsonNode s : svcs) {
                    ServiceDef def = new ServiceDef();
                    def.identifier = s.has("identifier") ? s.get("identifier").asText() : "";
                    def.name = s.has("name") ? s.get("name").asText() : "";
                    def.callType = s.has("callType") ? s.get("callType").asText() : "async";
                    def.inputData = parseParams(s.get("inputData"));
                    def.outputData = parseParams(s.get("outputData"));
                    model.services.add(def);
                }
            }

            JsonNode evts = root.get("events");
            if (evts != null && evts.isArray()) {
                for (JsonNode e : evts) {
                    EventDef def = new EventDef();
                    def.identifier = e.has("identifier") ? e.get("identifier").asText() : "";
                    def.name = e.has("name") ? e.get("name").asText() : "";
                    def.type = e.has("type") ? e.get("type").asText() : "info";
                    def.outputData = parseParams(e.get("outputData"));
                    model.events.add(def);
                }
            }
        } catch (Exception e) {
            System.err.println("解析物模型失败: " + e.getMessage());
        }
        return model;
    }

    private static List<ParamDef> parseParams(JsonNode node) {
        List<ParamDef> list = new ArrayList<>();
        if (node == null || !node.isArray()) return list;
        for (JsonNode p : node) {
            ParamDef def = new ParamDef();
            def.identifier = p.has("identifier") ? p.get("identifier").asText() : "";
            def.name = p.has("name") ? p.get("name").asText() : "";
            JsonNode dt = p.get("dataType");
            def.dataType = dt != null && dt.has("type") ? dt.get("type").asText() : "text";
            list.add(def);
        }
        return list;
    }

    public List<PropertyDef> getProperties() { return properties; }
    public List<ServiceDef> getServices() { return services; }
    public List<EventDef> getEvents() { return events; }

    public static class PropertyDef {
        public String identifier;
        public String name;
        public String accessMode;
        public String dataType;
        public double min;
        public double max;
        public double step;
        public String unit;
        public Map<String, String> enumValues;

        @Override
        public String toString() {
            return identifier + "(" + name + ") " + dataType + " [" + min + "~" + max + "]";
        }
    }

    public static class ServiceDef {
        public String identifier;
        public String name;
        public String callType;
        public List<ParamDef> inputData;
        public List<ParamDef> outputData;
    }

    public static class EventDef {
        public String identifier;
        public String name;
        public String type;
        public List<ParamDef> outputData;
    }

    public static class ParamDef {
        public String identifier;
        public String name;
        public String dataType;
    }
}
