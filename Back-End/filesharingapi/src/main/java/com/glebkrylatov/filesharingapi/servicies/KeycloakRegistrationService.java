package com.glebkrylatov.filesharingapi.servicies;

import com.glebkrylatov.filesharingapi.dtos.requests.RegisterRequest;
import com.glebkrylatov.filesharingapi.properties.KeycloakProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

/**
 * Сервис для регистрации пользователей в Keycloak
 * <p>
 * Класс выполняет создание нового пользователя в указанном realm Keycloak
 * через административный REST API. Для выполнения операции сервис сначала
 * получает административный access token, после чего отправляет запрос
 * на создание пользователя
 */
@Service
@RequiredArgsConstructor
public class KeycloakRegistrationService {

    /**
     * Данные подключения keycloak из application yaml
     */
    private final KeycloakProperties keycloakProperties;

    /**
     * Клиент для выполнения http-запросов к Keycloak
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Регистрирует нового пользователя в Keycloak
     * Метод получает административный токен, формирует JSON-объект пользователя
     * и отправляет POST-запрос в административный API Keycloak.
     * Пользователь создаётся активным, с подтверждённым email и постоянным паролем
     * @param request объект с регистрационными данными пользователя
     * @throws RuntimeException если пользователь с таким логином или email уже существует
     * @throws RuntimeException если Keycloak вернул ошибку при создании пользователя
     */
    public void registerUser(RegisterRequest request) {
        String adminToken = getAdminToken();

        String createUserUrl = keycloakProperties.getServerUrl()
                + "/admin/realms/"
                + keycloakProperties.getTargetRealm()
                + "/users";

        Map<String, Object> user = Map.of(
                "username", request.username(),
                "firstName", request.firstName(),
                "lastName", request.lastName(),
                "email", request.email(),
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(
                        Map.of(
                                "type", "password",
                                "value", request.password(),
                                "temporary", false
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(user, headers);

        try {
            restTemplate.exchange(createUserUrl, HttpMethod.POST, entity, Void.class);
        } catch (HttpClientErrorException.Conflict e) {
            throw new RuntimeException("Пользователь с таким логином или email уже существует");
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Ошибка: " + e.getResponseBodyAsString());
        }
    }

    /**
     * Получает административный access token от Keycloak
     * Метод отправляет запрос на token endpoint административного realm
     * Для получения токена используется password grant с client id администратора,
     * логином и паролем администратора, указанными в application.yaml
     * @return строковое значение access token администратора
     * @throws RuntimeException если Keycloak не вернул access_token
     */
    private String getAdminToken() {
        String tokenUrl = keycloakProperties.getServerUrl()
                + "/realms/"
                + keycloakProperties.getAdminRealm()
                + "/protocol/openid-connect/token";

        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", keycloakProperties.getAdminClientId());
        body.add("username", keycloakProperties.getAdminUsername());
        body.add("password", keycloakProperties.getAdminPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<LinkedMultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                entity,
                Map.class
        );

        if (response.getBody() == null || response.getBody().get("access_token") == null) {
            throw new RuntimeException("Не удалось получить admin token от Keycloak");
        }

        return response.getBody().get("access_token").toString();
    }
}
