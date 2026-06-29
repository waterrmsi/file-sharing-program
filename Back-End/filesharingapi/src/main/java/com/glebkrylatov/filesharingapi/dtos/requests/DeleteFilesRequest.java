package com.glebkrylatov.filesharingapi.dtos.requests;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record DeleteFilesRequest(
        List<UUID> fileIds
) { }
