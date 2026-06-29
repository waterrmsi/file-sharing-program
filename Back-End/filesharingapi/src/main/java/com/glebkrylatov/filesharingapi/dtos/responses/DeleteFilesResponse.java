package com.glebkrylatov.filesharingapi.dtos.responses;

import java.util.UUID;

public record DeleteFilesResponse(
    UUID id,
    boolean isDeleted,
    String operationMessage
) { }
