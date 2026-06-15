package com.glebkrylatov.filesharingapi.servicies;

import com.glebkrylatov.filesharingapi.dtos.requests.FileUploadRequest;
import com.glebkrylatov.filesharingapi.dtos.responses.PresignedUrlResponse;
import com.glebkrylatov.filesharingapi.repositories.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final StorageService storageService;

    public PresignedUrlResponse generatePresignedUrl(FileUploadRequest fileUploadRequest, String userId) {
        String key = userId + "/" + UUID.randomUUID().toString();
        String uploadUrl = storageService.generateUploadUrl(key, fileUploadRequest.contentType());

        return new PresignedUrlResponse(uploadUrl, key);
    }
}
