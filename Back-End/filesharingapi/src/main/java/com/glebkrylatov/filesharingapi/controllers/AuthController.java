package com.glebkrylatov.filesharingapi.controllers;

import com.glebkrylatov.filesharingapi.dtos.requests.RegisterRequest;
import com.glebkrylatov.filesharingapi.servicies.KeycloakRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
public class AuthController {

    private final KeycloakRegistrationService keycloakRegistrationService;

    /**
     * Эндпоинт регистрации пользователей в системе
     * @param request объект с регистрационными данными пользователя
     * @return ResponseEntity.ok - при регистрации, HttpStatus.CONFLICT при ошибке регистрации
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            keycloakRegistrationService.registerUser(request);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Пользователь успешно зарегистрирован"
                    )
            );

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of(
                            "success", false,
                            "message", e.getMessage()
                    )
            );
        }
    }
}