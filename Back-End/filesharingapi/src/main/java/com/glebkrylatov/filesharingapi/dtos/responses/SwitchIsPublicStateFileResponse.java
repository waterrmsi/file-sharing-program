package com.glebkrylatov.filesharingapi.dtos.responses;

import java.util.UUID;

public record SwitchIsPublicStateFileResponse(
        UUID fileId,
        boolean isSwitched,
        String message
) { }
