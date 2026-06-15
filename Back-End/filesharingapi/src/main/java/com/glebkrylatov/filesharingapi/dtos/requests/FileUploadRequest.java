package com.glebkrylatov.filesharingapi.dtos.requests;

public record FileUploadRequest (
    String fileName,
    String contentType,
    Long size
) { }
