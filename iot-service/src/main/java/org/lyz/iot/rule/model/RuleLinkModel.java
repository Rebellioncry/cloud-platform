package org.lyz.iot.rule.model;

import lombok.Data;

import java.util.Map;

@Data
public class RuleLinkModel {

    private String id;
    private String sourceId;
    private String targetId;
    private String sourceAnchor;
    private String targetAnchor;
    private String label;
    private Map<String, Object> properties;
}
