package org.lyz.iot.storage;

import org.lyz.iot.entity.IotFileStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileStorageFactory {

    private static final Map<String, FileStorage> CACHE = new ConcurrentHashMap<>();

    public static FileStorage getStorage(IotFileStorage config) {
        String key = config.getId();
        return CACHE.computeIfAbsent(key, k -> createStorage(config));
    }

    public static void removeCache(String storageId) {
        CACHE.remove(storageId);
    }

    private static FileStorage createStorage(IotFileStorage config) {
        if (config.getStorageType() == 1) {
            return new MinioFileStorage(
                    config.getEndpoint(),
                    config.getAccessKey(),
                    config.getSecretKey()
            );
        }
        throw new UnsupportedOperationException("不支持的存储类型: " + config.getStorageType());
    }
}
