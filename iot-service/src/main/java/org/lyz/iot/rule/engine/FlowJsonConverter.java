package org.lyz.iot.rule.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.lyz.iot.rule.model.RuleLinkModel;
import org.lyz.iot.rule.model.RuleModel;
import org.lyz.iot.rule.model.RuleNodeModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FlowJsonConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public RuleModel convert(String flowJson) {
        return convert(flowJson, null, null);
    }

    public RuleModel convert(String flowJson, String ruleId, String ruleName) {
        RuleModel model = new RuleModel();
        model.setId(ruleId);
        model.setName(ruleName);

        try {
            JsonNode root = objectMapper.readTree(flowJson);

            JsonNode nodesNode = root.get("nodes");
            if (nodesNode != null && nodesNode.isArray()) {
                for (JsonNode nodeNode : nodesNode) {
                    RuleNodeModel node = parseNode(nodeNode);
                    if (node != null) {
                        model.getNodes().add(node);
                    }
                }
            }

            JsonNode edgesNode = root.get("edges");
            if (edgesNode != null && edgesNode.isArray()) {
                for (JsonNode edgeNode : edgesNode) {
                    RuleLinkModel link = parseEdge(edgeNode);
                    if (link != null) {
                        model.getLinks().add(link);
                    }
                }
            }

            JsonNode configNode = root.get("configuration");
            if (configNode != null) {
                model.setConfiguration(objectMapper.convertValue(configNode, new TypeReference<Map<String, Object>>() {}));
            }

            log.info("FlowJson解析完成: {}个节点, {}个连接", model.getNodes().size(), model.getLinks().size());
        } catch (Exception e) {
            log.error("解析FlowJson失败", e);
            throw new RuntimeException("规则模型解析失败: " + e.getMessage());
        }

        return model;
    }

    private RuleNodeModel parseNode(JsonNode nodeNode) {
        RuleNodeModel node = new RuleNodeModel();
        node.setId(nodeNode.path("id").asText());
        node.setName(nodeNode.path("name").asText());
        node.setType(nodeNode.path("type").asText());

        String type = node.getType();
        if (type != null) {
            if (type.contains("trigger") || type.contains("device-event")) {
                node.setNodeType("trigger");
            } else if (type.contains("condition") || type.contains("script") || type.contains("data-transform")) {
                node.setNodeType("transform");
            } else if (type.equals("log") || type.equals("alarm")) {
                node.setNodeType("action");
            } else {
                node.setNodeType("sink");
            }
        }

        JsonNode propertiesNode = nodeNode.get("properties");
        if (propertiesNode != null) {
            node.setProperties(objectMapper.convertValue(propertiesNode, new TypeReference<Map<String, Object>>() {}));
        }

        return node;
    }

    private RuleLinkModel parseEdge(JsonNode edgeNode) {
        RuleLinkModel link = new RuleLinkModel();
        link.setId(edgeNode.path("id").asText());

        String sourceId = edgeNode.path("sourceNodeId").asText(null);
        if (sourceId == null || sourceId.isEmpty()) {
            sourceId = edgeNode.path("source").asText(null);
        }
        link.setSourceId(sourceId);

        String targetId = edgeNode.path("targetNodeId").asText(null);
        if (targetId == null || targetId.isEmpty()) {
            targetId = edgeNode.path("target").asText(null);
        }
        link.setTargetId(targetId);

        String sourceAnchor = edgeNode.path("sourceAnchorId").asText(null);
        if (sourceAnchor == null) {
            sourceAnchor = edgeNode.path("sourceAnchor").asText(null);
        }
        link.setSourceAnchor(sourceAnchor);

        String targetAnchor = edgeNode.path("targetAnchorId").asText(null);
        if (targetAnchor == null) {
            targetAnchor = edgeNode.path("targetAnchor").asText(null);
        }
        link.setTargetAnchor(targetAnchor);

        link.setLabel(edgeNode.path("label").asText(null));
        return link;
    }

    public String toJson(RuleModel model) {
        try {
            Map<String, Object> flow = new HashMap<>();
            flow.put("nodes", model.getNodes());
            flow.put("edges", model.getLinks());
            flow.put("configuration", model.getConfiguration());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(flow);
        } catch (Exception e) {
            throw new RuntimeException("规则模型序列化失败", e);
        }
    }
}
