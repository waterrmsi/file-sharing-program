package com.glebkrylatov.filesharingapi.dtos.responses;

public record FileUploadConfirmResponse(
    String key,
    String fileName,
    long size
) { }
