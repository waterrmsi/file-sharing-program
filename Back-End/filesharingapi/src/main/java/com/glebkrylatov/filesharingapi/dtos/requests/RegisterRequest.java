package com.glebkrylatov.filesharingapi.dtos.requests;

public record RegisterRequest(
        String username,
        String firstName,
        String lastName,
        String email,
        String password
) { }
