package com.glebkrylatov.filesharingapi.servicies;

import com.glebkrylatov.filesharingapi.models.User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    /**
     * Преобразует jwt-token в объект класса User
     * @param token jwt-token авторизации
     * @return Модель данных пользователя
     */
    public User getUserFromToken(JwtAuthenticationToken token) {
        User user = new User();
        user.setId(token.getToken().getSubject());
        user.setLogin(token.getToken().getClaimAsString("username"));
        user.setEmail(token.getToken().getClaimAsString("email"));
        user.setFirstName(token.getToken().getClaimAsString("first_name"));
        user.setLastName(token.getToken().getClaimAsString("last_name"));

        return user;
    }
}
