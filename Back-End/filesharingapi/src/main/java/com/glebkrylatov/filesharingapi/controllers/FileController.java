package com.glebkrylatov.filesharingapi.controllers;

import com.glebkrylatov.filesharingapi.dtos.requests.DeleteFileRequest;
import com.glebkrylatov.filesharingapi.dtos.requests.FileUploadConfirmRequest;
import com.glebkrylatov.filesharingapi.dtos.requests.FileUploadRequest;
import com.glebkrylatov.filesharingapi.dtos.responses.DeleteFileResponse;
import com.glebkrylatov.filesharingapi.dtos.responses.FileUploadConfirmResponse;
import com.glebkrylatov.filesharingapi.dtos.responses.PresignedUrlResponse;
import com.glebkrylatov.filesharingapi.dtos.responses.UserFileResponse;
import com.glebkrylatov.filesharingapi.servicies.FileService;
import com.glebkrylatov.filesharingapi.servicies.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Documented;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "Файлы", description = "Контроллер для работы с файлами.")
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

    @Operation(summary = "Подтверждение загрузки файла в Minio хранилища",
            description = "Добавляет данные о файле в БД")
    @PostMapping("/confirm-upload-file")
    public ResponseEntity<FileUploadConfirmResponse> confirmUploadFile (@RequestBody FileUploadConfirmRequest request, Authentication authentication) {
        String userId = userService.getUserFromToken((JwtAuthenticationToken) authentication).getId();

        if(fileService.existFileByKey(request.key())) {
            UUID fileId = fileService.createFileData(request, userId);

            return ResponseEntity.ok(new FileUploadConfirmResponse(fileId));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Удаление файла",
            description = "Удаляет данные с Minio Хранилища и с БД")
    @DeleteMapping("delete-file")
    public ResponseEntity<DeleteFileResponse> deleteFile(@RequestBody DeleteFileRequest request, Authentication authentication) {
        String userId = userService.getUserFromToken((JwtAuthenticationToken) authentication).getId();

        DeleteFileResponse response = fileService.deleteFile(request, userId);

        if(!response.isDeleted()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение данных о файлах пользователя",
            description = "Получение данных о файлах пользователя с бд, для отображение на фронте")
    @GetMapping("user-files")
    public ResponseEntity<List<UserFileResponse>> getUserFiles(Authentication authentication) {
        String userId = userService.getUserFromToken((JwtAuthenticationToken) authentication).getId();

        List<UserFileResponse> result = fileService.getUserFiles(userId);

        if(result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Получение данных о файле по его ID",
            description = "Получение данных о файле с бд, для отображение на фронте")
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
}
