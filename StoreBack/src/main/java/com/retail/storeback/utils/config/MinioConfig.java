package com.retail.storeback.utils.config;

import com.retail.storeback.utils.MinioProperties;
import com.retail.storeback.utils.exceptions.MinioConnectionException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@AllArgsConstructor
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        try {

            MinioClient client = MinioClient.builder()
                    .endpoint(minioProperties.endpoint())
                    .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                    .build();

            ensureBucketExists(client, minioProperties.bucketName());
            log.info("Successfully connect to Minio Storage to endpoint {}", minioProperties.endpoint());

            return client;
        } catch (MinioException e) {
            throw handleConnectionException("Error create MinIO client", e);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw handleConnectionException("Authenticated error connect to MinIO", e);
        } catch (Exception e) {
            throw handleConnectionException("Can not connect to MinIO server", e);
        }
    }

    private void ensureBucketExists(MinioClient client, String bucketName) throws Exception {
        boolean found = client.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build()
        );

        if (!found) {
            client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("Created new bucket: {}", bucketName);
        }
    }

    private MinioConnectionException handleConnectionException(String message, Throwable e) {
        log.error("{}: {}", message, e.getMessage(), e);
        return new MinioConnectionException(message + " - " + e.getMessage(), e);
    }
}
