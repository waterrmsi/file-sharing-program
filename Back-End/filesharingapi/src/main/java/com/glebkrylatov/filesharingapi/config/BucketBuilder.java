package com.glebkrylatov.filesharingapi.config;

import com.glebkrylatov.filesharingapi.properties.MinioProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Component
@RequiredArgsConstructor
public class BucketBuilder {
    private final MinioProperties minioProperties;
    private final S3Client s3Client;

    @PostConstruct
    public void initBucket() {
        String bucket = minioProperties.getBucket();

        try {
            s3Client.headBucket(HeadBucketRequest
                    .builder()
                    .bucket(bucket)
                    .build());
            System.out.println("Bucket already exists: " + bucket);

        } catch (Exception e) {
            s3Client.createBucket(CreateBucketRequest
                    .builder()
                    .bucket(bucket)
                    .build());
            System.out.println("Bucket created: " + bucket);
        }
    }
}
