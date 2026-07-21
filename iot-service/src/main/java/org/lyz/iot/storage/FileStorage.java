package org.lyz.iot.storage;

import java.io.InputStream;

public interface FileStorage {
    String upload(String bucket, String objectName, InputStream inputStream, long size, String contentType);
    String getPresignedUrl(String bucket, String objectName, int expireSeconds);
    void delete(String bucket, String objectName);
    boolean testConnection();
}
