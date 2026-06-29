package com.glebkrylatov.filesharingapi.controllers;

import com.glebkrylatov.filesharingapi.dtos.requests.*;
import com.glebkrylatov.filesharingapi.dtos.responses.*;
import com.glebkrylatov.filesharingapi.servicies.FileService;
import com.glebkrylatov.filesharingapi.servicies.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "Файлы", description = "Контроллер для работы с файлами.")
@CrossOrigin(origins = "http://127.0.0.1:5500") //web-server
public class FileController  {
    private final FileService fileService;
    private final UserService userService;

    @Operation(summary = "Получить presigned-url для загрузки данных напрямую в Minio",
            description = "Возвращает ссылку на добавление файла в Minio хранилище")
    @PostMapping("/generate-upload-url")
    public ResponseEntity<PresignedUrlResponse> generateUploadUrl
            (@RequestBody FileUploadRequest request,
             Authentication authentication) {
        String userId = userService.getUserFromToken((JwtAuthenticationToken) authentication).getId();

        return ResponseEntity.ok(fileService.generateUploadPresignedUrl(request, userId));
    }

    @PostMapping("generate-download-url")
    public ResponseEntity<GenerateDownloadUrlResponse> generateDownloadUrl(@RequestBody GenerateDownloadUrlRequest request, Authentication authentication) {
        String userId = userService.getUserFromToken((JwtAuthenticationToken) authentication).getId();

        GenerateDownloadUrlResponse result = fileService.generateDownloadUrlResponse(request, userId);

        if(result.url().isEmpty()) {
            return ResponseEntity.notFound().build();
        };

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Подтверждение загрузки файла в Minio хранилища",
            description = "Добавляет данные о файле в БД")
    @PostMapping("/confirm-upload-file")
    public ResponseEntity<UserFileResponse> confirmUploadFile (@RequestBody FileUploadConfirmRequest request, Authentication authentication) {
        String userId = userService.getUserFromToken((JwtAuthenticationToken) authentication).getId();

        if(fileService.existFileByKey(request.objectKey())) {
            UserFileResponse result = fileService.createFileData(request, userId);

            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Удаление файла",
            description = "Удаляет данные с Minio Хранилища и с БД")
    @DeleteMapping("delete-file")
    public ResponseEntity<DeleteFileResponse> deleteFile(@RequestBody DeleteFileRequest request, Authentication authentication) {
        String userId = userService.getUserFromToken((JwtAuthenticationToken) authentication).getId();

        DeleteFileResponse response = fileService.deleteFile(request.fileId(), userId);

        if(!response.isDeleted()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение данных о файлах пользователя",
            description = "Получение данных о файлах пользователя с бд")
    @GetMapping("user-files")
    public ResponseEntity<List<UserFileResponse>> getUserFiles(Authentication authentication) {
        String userId = userService.getUserFromToken((JwtAuthenticationToken) authentication).getId();

        List<UserFileResponse> result = fileService.getUserFiles(userId);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Получение данных о файле по его ID",
            description = "Получение данных о файле с бд")
    @GetMapping("file-by-id")
    public ResponseEntity<UserFileResponse> getFileById(@RequestParam UUID fileId,
                                                        Authentication authentication) {
        String userId = userService.getUserFromToken((JwtAuthenticationToken) authentication).getId();

        UserFileResponse file = fileService.getFileById(fileId, userId);

        if(file == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(file);
    }

    @Operation(summary = "Удаление списка файлов по их ID",
            description = "Удаляет список файлов из бд и S3 хранилища")
    @DeleteMapping("delete-files")
    public ResponseEntity<List<DeleteFilesResponse>> deleteFile(@RequestBody DeleteFilesRequest request, Authentication authentication) {
        String userId = userService.getUserFromToken((JwtAuthenticationToken) authentication).getId();
        List<DeleteFilesResponse> result = fileService.deleteFiles(request, userId);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Установка состояния публичности файла",
            description = "Устанавливает isPublic по fileId")
    @PostMapping("switch-file-state")
    public ResponseEntity<SwitchIsPublicStateFileResponse> switchIsPublicStateOnFile(@RequestBody SwitchIsPublicStateFileRequest request,
                                                      Authentication authentication) {
        String userId = userService.getUserFromToken((JwtAuthenticationToken) authentication).getId();

        SwitchIsPublicStateFileResponse result = fileService.switchIsPublicStateOnFile(request,  userId);

        if(result.isSwitched()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
