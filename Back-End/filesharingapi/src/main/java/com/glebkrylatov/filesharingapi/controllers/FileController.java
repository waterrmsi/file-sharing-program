package com.glebkrylatov.filesharingapi.controllers;

import com.glebkrylatov.filesharingapi.dtos.requests.FileUploadRequest;
import com.glebkrylatov.filesharingapi.dtos.responses.FileUploadConfirmResponse;
import com.glebkrylatov.filesharingapi.dtos.responses.PresignedUrlResponse;
import com.glebkrylatov.filesharingapi.servicies.FileService;
import com.glebkrylatov.filesharingapi.servicies.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

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

    /*@PostMapping("/confirm-upload-file")
    public ResponseEntity<FileUploadConfirmResponse> confirmUploadFile (@RequestBody FileUploadRequest request,
                                                                        Authentication authentication) {
        return ResponseEntity.ok(new FileUploadConfirmResponse());
    }*/
}
