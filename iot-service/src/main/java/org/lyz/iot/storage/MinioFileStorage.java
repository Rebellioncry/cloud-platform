package org.lyz.iot.storage;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MinioFileStorage implements FileStorage {

    private final MinioClient client;
    private final String endpoint;
    private final String accessKey;
    private final String secretKey;

    public MinioFileStorage(String endpoint, String accessKey, String secretKey) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Override
    public String upload(String bucket, String objectName, InputStream inputStream, long size, String contentType) {
        try {
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("创建MinIO Bucket: {}", bucket);
            }
            client.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
            log.info("文件上传成功: bucket={}, object={}", bucket, objectName);
            return objectName;
        } catch (Exception e) {
            throw new RuntimeException("MinIO上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getPresignedUrl(String bucket, String objectName, int expireSeconds) {
        try {
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucket)
                    .object(objectName)
                    .expiry(expireSeconds, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO获取预签名URL失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String bucket, String objectName) {
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            log.info("文件删除成功: bucket={}, object={}", bucket, objectName);
        } catch (Exception e) {
            throw new RuntimeException("MinIO删除失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean testConnection() {
        try {
            client.bucketExists(BucketExistsArgs.builder().bucket("test-connection-check").build());
            return true;
        } catch (Exception e) {
            log.error("MinIO连接测试失败: {}", e.getMessage());
            return false;
        }
    }

    public MinioClient getClient() {
        return client;
    }
}
