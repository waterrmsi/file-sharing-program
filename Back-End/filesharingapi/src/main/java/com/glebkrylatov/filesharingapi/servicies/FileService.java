package com.glebkrylatov.filesharingapi.servicies;

import com.glebkrylatov.filesharingapi.dtos.requests.*;
import com.glebkrylatov.filesharingapi.dtos.responses.*;
import com.glebkrylatov.filesharingapi.models.File;
import com.glebkrylatov.filesharingapi.repositories.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final StorageService storageService;

    /**
    * Генерирует key файла, передаёт его в storage service для генерации presigned-url
    * @param fileUploadRequest dto
    * @param userId - id пользователя
    * @return presigned-url, key файла
    */
    public PresignedUrlResponse generateUploadPresignedUrl(FileUploadRequest fileUploadRequest, String userId) {
        String key = userId + "/" + UUID.randomUUID().toString();
        String uploadUrl = storageService.generateUploadUrl(key, fileUploadRequest.contentType());

        return new PresignedUrlResponse(uploadUrl, key);
    }

    public GenerateDownloadUrlResponse generateDownloadUrlResponse(GenerateDownloadUrlRequest request,
            String userId) {
        Optional<File> optional = fileRepository.getFileById(request.fileId());

        if(optional.isEmpty()) {
            return new GenerateDownloadUrlResponse(null);
        }

        File file = optional.get();

        if(!file.getOwnerId().equals(userId) && !file.isPublic()) {
            return new GenerateDownloadUrlResponse(null);
        }

        return new GenerateDownloadUrlResponse(storageService.generateDownloadUrl(file.getObjectKey()));
    }

    /**
     *
     * @param request dto
     * @param userId id пользователя
     * @return
     */
    public UserFileResponse createFileData(FileUploadConfirmRequest request, String userId) {
        File file = new File();
        file.setFileName(request.fileName());
        file.setContentType(request.contentType());
        file.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        file.setBucket(storageService.getBucketName());
        file.setPublic(request.isPublic());
        file.setSize(request.size());
        file.setOwnerId(userId);
        file.setObjectKey(request.objectKey());

        return this.convertFileToDto(fileRepository.save(file));
    }

    /**
     * Возвращает информацию о существовании файла в s3 хранилище
     * @param key ключ файла в s3
     * @return true если файл существует в s3, false если не существует
     */
    public boolean existFileByKey(String key) {
        return storageService.existFileByKey(key);
    }

    /**
     * Удаляет файл из бд и s3 хранилища по его id
     * @param id id файла
     * @param userId id пользователя
     * @return dto объект с информацией об удалении файла
     */
    public DeleteFileResponse deleteFile(UUID id, String userId) {
        Optional<File> optional = fileRepository.getFileById(id);
        if(optional.isEmpty()) {
            return new DeleteFileResponse(false, "Файл не найден");
        }

        File file = optional.get();

        if(!file.getOwnerId().equals(userId)) {
            return new DeleteFileResponse(false, "Вы не можете удалить чужой файл");
        }

        //Если существует запись в бд, но файл в S3 не найден, то удаляем запись в бд.
        if(!storageService.deleteFileFromStorage(file.getObjectKey())) {
            fileRepository.delete(file);

            return new DeleteFileResponse(false, "Файл не найден");
        }

        fileRepository.delete(file);

        return new DeleteFileResponse(true, "Файл успешно удален");
    }

    /**
     * Получает данные файлов с бд
     * @param userId id пользователя
     * @return dto объект - список данных файлов
     */
    public List<UserFileResponse> getUserFiles(String userId) {
        List<File> files = fileRepository.getFilesByOwnerId(userId);
        List<UserFileResponse> result = new ArrayList<>();

        for(File file : files) {
            result.add(new UserFileResponse(
                    file.getId(),
                    file.getFileName(),
                    file.getContentType(),
                    file.getSize(),
                    file.isPublic(),
                    file.getCreatedAt().toString())
            );
        }

        return result;
    }

    /**
     * Получает файл по id
     * @param Id id файла
     * @param userId id пользователя
     * @return dto объект с данными файла
     */
    public UserFileResponse getFileById(UUID Id, String userId) {
        Optional<File> optional = fileRepository.getFileById(Id);

        if(optional.isEmpty()) {
            return null;
        }

        File file = optional.get();

        //Если файл не публичный и его запрашивает другой пользователь, то not found
        if(!file.getOwnerId().equals(userId) && !file.isPublic()) {
            return null;
        }

        return this.convertFileToDto(file);
    }

    /**
     * Преобразует Entity файла в dto
     * @param file Entity файла
     * @return dto
     */
    private UserFileResponse convertFileToDto(File file) {
        UserFileResponse result = new UserFileResponse(
                file.getId(),
                file.getFileName(),
                file.getContentType(),
                file.getSize(),
                file.isPublic(),
                file.getCreatedAt().toString()
        );

        return result;
    }

    /**
     * Удаляет один или несколько файлов из БД и s3 хранилища по их id
     * @param request dto
     * @param userId id пользователя
     * @return dto с информацией об удалении файлов
     */
    public List<DeleteFilesResponse> deleteFiles(DeleteFilesRequest request, String userId) {
        List<DeleteFilesResponse> result = new ArrayList<>();

        for(UUID id : request.fileIds()) {
            DeleteFileResponse response = this.deleteFile(id, userId);
            result.add(new DeleteFilesResponse(id, response.isDeleted(), response.operationMessage()));
        }

        return result;
    }

    /**
     * Устанавливает состояние публичности файла
     * @param request dto
     * @param userId id пользователя
     * @return dto с информацией об изменении состояния публичности файла
     */
    public SwitchIsPublicStateFileResponse switchIsPublicStateOnFile(SwitchIsPublicStateFileRequest request, String userId) {
        Optional<File> optional = fileRepository.getFileById(request.id());

        if(optional.isEmpty()) {
            return new SwitchIsPublicStateFileResponse(request.id(), false, "Файл не найден");
        }

        File file = optional.get();

        if(!file.getOwnerId().equals(userId)) {
            return new SwitchIsPublicStateFileResponse(request.id(),
                    false,
                    "Вы не можете изменить публичность чужого файла");
        }

        file.setPublic(request.isPublic());

        fileRepository.save(file);

        return new SwitchIsPublicStateFileResponse(request.id(), true, "Успешно");
    }
}