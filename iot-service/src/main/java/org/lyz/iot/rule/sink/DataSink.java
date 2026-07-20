package org.lyz.iot.rule.sink;

public interface DataSink {

    String getType();

    void init(DataSinkConfig config);

    void send(DataSinkMessage message);

    void close();

    boolean isAlive();
}
