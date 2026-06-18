package com.glebkrylatov.filesharingapi.servicies;

import com.glebkrylatov.filesharingapi.dtos.requests.DeleteFileRequest;
import com.glebkrylatov.filesharingapi.dtos.requests.FileUploadConfirmRequest;
import com.glebkrylatov.filesharingapi.dtos.requests.FileUploadRequest;
import com.glebkrylatov.filesharingapi.dtos.responses.DeleteFileResponse;
import com.glebkrylatov.filesharingapi.dtos.responses.PresignedUrlResponse;
import com.glebkrylatov.filesharingapi.dtos.responses.UserFileResponse;
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

    public PresignedUrlResponse generateUploadPresignedUrl(FileUploadRequest fileUploadRequest, String userId) {
        String key = userId + "/" + UUID.randomUUID().toString();
        String uploadUrl = storageService.generateUploadUrl(key, fileUploadRequest.contentType());

        return new PresignedUrlResponse(uploadUrl, key);
    }

    public UUID createFileData(FileUploadConfirmRequest request, String userId) {
        File file = new File();
        file.setFileName(request.fileName());
        file.setContentType(request.contentType());
        file.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        file.setBucket(storageService.getBucketName());
        file.setPublic(request.isPublic());
        file.setSize(request.size());
        file.setOwnerId(userId);

        return fileRepository.save(file).getId();
    }

    public boolean existFileByKey(String key) {
        return storageService.existFileByKey(key);
    }

    public DeleteFileResponse deleteFile(DeleteFileRequest request, String userId) {
        Optional<File> optional = fileRepository.getFileById(request.fileId());
        if(optional.isEmpty()) {
            return new DeleteFileResponse(false, "Файл не найден");
        }

        File file = optional.get();

        if(!file.getOwnerId().equals(userId)) {
            return new DeleteFileResponse(false, "Вы не можете удалить свой файл");
        }

        //Если существует запись в бд, но файл в S3 не найден, то удаляем запись в бд.
        if(!storageService.deleteFileFromStorage(file.getObjectKey())) {
            fileRepository.delete(file);

            return new DeleteFileResponse(false, "Файл не найден");
        }

        fileRepository.delete(file);

        return new DeleteFileResponse(true, "Файл успешно удален");
    }

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

        return new UserFileResponse(
                file.getId(),
                file.getFileName(),
                file.getContentType(),
                file.getSize(),
                file.isPublic(),
                file.getCreatedAt().toString());
    }
}