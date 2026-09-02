package com.retail.storeback.product.service;

import com.retail.storeback.utils.MinioProperties;
import io.minio.*;
import io.minio.messages.Item;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class S3Service {

    @Autowired
    private MinioClient minioClient;

    private final MinioProperties minioProperties;

    public void createdBucketIfNotExisted() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(minioProperties.bucketName())
                .build()
        );
        if (!found) {
             minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.bucketName()).build());
        }
    }

    public String uploadFile(MultipartFile file) throws Exception {

        createdBucketIfNotExisted();

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioProperties.bucketName())
                        .object(fileName)
                        .stream(file.getInputStream(), file.getSize(), (long) -1)
                        .build()
        );
    
        return fileName;
    }

    public InputStream downloadFile(String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioProperties.bucketName())
                        .object(fileName)
                        .build()
        );
    }

    public String getFileUrl(String fileName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Http.Method.GET)
                        .bucket(minioProperties.bucketName())
                        .object(fileName)
                        .expiry(60 * 60 * 24)
                        .build()
        );
    }

    public void deleteFile(String fileName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(minioProperties.bucketName())
                        .object(fileName)
                        .build()
        );
    }

    public List<String> listFiles() throws Exception {
        List<String> fileNames = new ArrayList<>();
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(minioProperties.bucketName()).build()
        );

        for (Result<Item> result : results) {
            fileNames.add(result.get().objectName());
        }

        return fileNames;
    }
}
