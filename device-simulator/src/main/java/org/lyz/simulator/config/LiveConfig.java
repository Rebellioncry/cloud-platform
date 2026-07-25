package org.lyz.simulator.config;

import javafx.beans.property.*;

public class LiveConfig {

    private final StringProperty deviceName = new SimpleStringProperty("");
    private final IntegerProperty propertyInterval = new SimpleIntegerProperty(5000);
    private final BooleanProperty otaEnabled = new SimpleBooleanProperty(true);
    private final BooleanProperty propertyReportEnabled = new SimpleBooleanProperty(true);
    private final StringProperty firmwareVersion = new SimpleStringProperty("1.0.0");
    private final IntegerProperty otaVersionInterval = new SimpleIntegerProperty(30000);
    private final IntegerProperty otaProgressInterval = new SimpleIntegerProperty(1000);

    public String getDeviceName() { return deviceName.get(); }
    public void setDeviceName(String v) { deviceName.set(v); }
    public StringProperty deviceNameProperty() { return deviceName; }

    public int getPropertyInterval() { return propertyInterval.get(); }
    public void setPropertyInterval(int v) { propertyInterval.set(v); }
    public IntegerProperty propertyIntervalProperty() { return propertyInterval; }

    public boolean isOtaEnabled() { return otaEnabled.get(); }
    public void setOtaEnabled(boolean v) { otaEnabled.set(v); }
    public BooleanProperty otaEnabledProperty() { return otaEnabled; }

    public boolean isPropertyReportEnabled() { return propertyReportEnabled.get(); }
    public void setPropertyReportEnabled(boolean v) { propertyReportEnabled.set(v); }
    public BooleanProperty propertyReportEnabledProperty() { return propertyReportEnabled; }

    public String getFirmwareVersion() { return firmwareVersion.get(); }
    public void setFirmwareVersion(String v) { firmwareVersion.set(v); }
    public StringProperty firmwareVersionProperty() { return firmwareVersion; }

    public int getOtaVersionInterval() { return otaVersionInterval.get(); }
    public void setOtaVersionInterval(int v) { otaVersionInterval.set(v); }
    public IntegerProperty otaVersionIntervalProperty() { return otaVersionInterval; }

    public int getOtaProgressInterval() { return otaProgressInterval.get(); }
    public void setOtaProgressInterval(int v) { otaProgressInterval.set(v); }
    public IntegerProperty otaProgressIntervalProperty() { return otaProgressInterval; }
}
