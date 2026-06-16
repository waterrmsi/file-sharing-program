package com.glebkrylatov.filesharingapi.controllers;

import com.glebkrylatov.filesharingapi.dtos.requests.DeleteFileRequest;
import com.glebkrylatov.filesharingapi.dtos.requests.FileUploadConfirmRequest;
import com.glebkrylatov.filesharingapi.dtos.requests.FileUploadRequest;
import com.glebkrylatov.filesharingapi.dtos.responses.DeleteFileResponse;
import com.glebkrylatov.filesharingapi.dtos.responses.FileUploadConfirmResponse;
import com.glebkrylatov.filesharingapi.dtos.responses.PresignedUrlResponse;
import com.glebkrylatov.filesharingapi.models.User;
import com.glebkrylatov.filesharingapi.servicies.FileService;
import com.glebkrylatov.filesharingapi.servicies.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController  {
    private final FileService fileService;
    private final UserService userService;
    
    @PostMapping("/generate-upload-url")
    public ResponseEntity<PresignedUrlResponse> generateUploadUrl
            (@RequestBody FileUploadRequest request,
             Authentication authentication) {
        String userId = userService.getUserFromToken((JwtAuthenticationToken) authentication).getId();
        return ResponseEntity.ok(fileService.generatePresignedUrl(request, userId));
    }

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

    @DeleteMapping("delete-file")
    public ResponseEntity<DeleteFileResponse> deleteFile(@RequestBody DeleteFileRequest request, Authentication authentication) {
        String userId = userService.getUserFromToken((JwtAuthenticationToken) authentication).getId();


    }
}
