package com.glebkrylatov.filesharingapi.dtos.responses;

public record DeleteFileResponse (
    boolean isDeleted,
    String operationMessage
) { }
