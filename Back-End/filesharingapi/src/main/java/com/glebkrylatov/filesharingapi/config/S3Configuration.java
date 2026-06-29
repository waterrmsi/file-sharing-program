package com.glebkrylatov.filesharingapi.config;

import com.glebkrylatov.filesharingapi.properties.MinioProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Класс для инициализации объектов S3 хранилища
 */
@Configuration
@RequiredArgsConstructor
public class S3Configuration {
    /**
     * Данные подключения к Minio хранилищу из application.yaml
     */
    private final MinioProperties minioProperties;

    /**
     * Инициализирует клиент для работы с Minio
     * @return объект класса - S3Client
     */
    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                minioProperties.getAccessKey(),
                minioProperties.getSecretKey()
        );

        return S3Client.builder()
                .endpointOverride(URI.create(minioProperties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("us-east-1"))
                .forcePathStyle(true)
                .build();
    }

    /**
     * Метод инициализирующий объект для генерации временных ссылок на загрузку или скачивания файлов с S3 хранилища
     * @return объект класса - S3Presigner
     */
    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                minioProperties.getAccessKey(),
                minioProperties.getSecretKey()
        );

        return S3Presigner.builder()
                .endpointOverride(URI.create(minioProperties.getPublicEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("us-east-1"))
                .serviceConfiguration(
                        software.amazon.awssdk.services.s3.S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build()
                )
                .build();
    }
}
