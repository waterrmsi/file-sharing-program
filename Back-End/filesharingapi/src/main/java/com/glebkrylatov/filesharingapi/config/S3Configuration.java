package com.glebkrylatov.filesharingapi.config;

import com.glebkrylatov.filesharingapi.properties.MinioProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class S3Configuration {
    private final MinioProperties minioProperties;

    @Bean
    public S3Client s3Configuration() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                minioProperties.getAccessKey(),
                minioProperties.getSecretKey()
        );

        return S3Client.builder()
                .endpointOverride(URI.create(minioProperties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(""))
                .forcePathStyle(true)
                .build();
    }
}
