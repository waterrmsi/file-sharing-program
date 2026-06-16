package com.glebkrylatov.filesharingapi.dtos.requests;

public record FileUploadConfirmRequest (
        String fileName,
        String objectKey,
        String contentType,
        long size,
        boolean isPublic,
        String key
) { }
