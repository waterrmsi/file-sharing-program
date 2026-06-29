package com.glebkrylatov.filesharingapi.dtos.requests;

import java.util.UUID;

public record GenerateDownloadUrlRequest(
    UUID fileId
) { }
