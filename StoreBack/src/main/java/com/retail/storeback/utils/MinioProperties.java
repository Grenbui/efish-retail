package com.retail.storeback.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "minio")
public record MinioProperties (
        String endpoint,
        String accessKey,
        String secretKey,
        String bucketName,
        boolean secure
){

}
