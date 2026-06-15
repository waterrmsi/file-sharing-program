package com.glebkrylatov.filesharingapi.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public record PresignedUrlResponse (
    String presignedUrl,
    String key
) { }
