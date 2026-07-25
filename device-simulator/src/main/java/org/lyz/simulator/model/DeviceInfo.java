package org.lyz.simulator.model;

import java.util.ArrayList;
import java.util.List;

public class DeviceInfo {

    private String id;
    private String productKey;
    private String deviceName;
    private String deviceKey;
    private String firmwareVersion;
    private String thingModelJson;
    private ThingModel thingModel;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductKey() { return productKey; }
    public void setProductKey(String productKey) { this.productKey = productKey; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getDeviceKey() { return deviceKey; }
    public void setDeviceKey(String deviceKey) { this.deviceKey = deviceKey; }
    public String getFirmwareVersion() { return firmwareVersion; }
    public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }
    public String getThingModelJson() { return thingModelJson; }
    public void setThingModelJson(String thingModelJson) { this.thingModelJson = thingModelJson; }
    public ThingModel getThingModel() { return thingModel; }
    public void setThingModel(ThingModel thingModel) { this.thingModel = thingModel; }
}
