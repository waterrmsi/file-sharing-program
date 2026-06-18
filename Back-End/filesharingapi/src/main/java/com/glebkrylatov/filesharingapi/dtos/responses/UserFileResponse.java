package com.glebkrylatov.filesharingapi.dtos.responses;

import java.util.UUID;

public record UserFileResponse(
    UUID id,
    String filename,
    String contentType,
    Long size,
    boolean isPublic,
    String createdAt
) {}
