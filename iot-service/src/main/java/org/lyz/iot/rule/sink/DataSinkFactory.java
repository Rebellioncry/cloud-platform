package org.lyz.iot.rule.sink;

public interface DataSinkFactory {

    String getType();

    DataSink getInstance(DataSinkConfig config);
}
