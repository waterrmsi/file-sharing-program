package com.glebkrylatov.filesharingapi.dtos.requests;

import java.util.UUID;

public record SwitchIsPublicStateFileRequest(
        UUID id,
        boolean isPublic
) { }
