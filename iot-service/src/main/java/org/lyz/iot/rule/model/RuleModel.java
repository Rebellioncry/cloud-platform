package org.lyz.iot.rule.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RuleModel {

    private String id;
    private String name;
    private String description;
    private Integer ruleType;
    private List<RuleNodeModel> nodes = new ArrayList<>();
    private List<RuleLinkModel> links = new ArrayList<>();
    private Map<String, Object> configuration = new HashMap<>();

    public RuleNodeModel getNodeById(String nodeId) {
        if (nodeId == null || nodes == null) return null;
        for (RuleNodeModel node : nodes) {
            if (nodeId.equals(node.getId())) {
                return node;
            }
        }
        return null;
    }

    public List<RuleNodeModel> getNextNodes(String nodeId) {
        List<RuleNodeModel> next = new ArrayList<>();
        if (links == null) return next;
        for (RuleLinkModel link : links) {
            if (nodeId.equals(link.getSourceId())) {
                RuleNodeModel targetNode = getNodeById(link.getTargetId());
                if (targetNode != null) {
                    next.add(targetNode);
                }
            }
        }
        return next;
    }

    public RuleNodeModel getTriggerNode() {
        if (nodes == null) return null;
        for (RuleNodeModel node : nodes) {
            if (node.isTrigger()) {
                return node;
            }
        }
        return null;
    }
}
