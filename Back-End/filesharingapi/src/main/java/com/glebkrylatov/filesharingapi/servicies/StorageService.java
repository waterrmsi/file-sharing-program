package com.glebkrylatov.filesharingapi.servicies;

import com.glebkrylatov.filesharingapi.properties.MinioProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

//Сервис для работы с S3 хранилищем (Minio)
@Service
@RequiredArgsConstructor
public class StorageService {
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final MinioProperties minioProperties;

    //Возвращает ссылку для загрузки файла напрямую в Minio хранилище. Время жизни ссылки 5 минут
    public String generateUploadUrl(String key, String contentType) {
        return s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .putObjectRequest(PutObjectRequest.builder()
                                .bucket(minioProperties.getBucket())
                                .key(key)
                                .contentType(contentType)
                                .build())
                        .build())
                .url()
                .toString();
    }

    //Возвращает ссылку для загрузки файла напрямую в Minio хранилище. Время жизни 5 минут
    public String generateDownloadUrl(String key) {
        return s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .getObjectRequest(GetObjectRequest.builder()
                                .bucket(minioProperties.getBucket())
                                .key(key)
                                .build())
                        .build())
                .url()
                .toString();
    }

    //удаляет файл из minio по ключу
    public boolean deleteFileFromStorage(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().key(key).build());
        return true;
    }
}
