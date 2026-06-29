package com.glebkrylatov.filesharingapi.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@SpringBootTest
public class FileControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    /**
     * Проверяет эндпоинт получения файлов пользователя
     * возвращает 401 без авторизации
     */
    @Test
    void test1() throws Exception {
        mockMvc.perform(get("/files/user-files"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет эндпоинт получения файла по ID
     * возвращает 401 без авторизации
     */
    @Test
    void test2() throws Exception {
        mockMvc.perform(get("/files/file-by-id")
                        .param("fileId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет эндпоинт удаления файла
     * возвращает 401 без авторизации
     */
    @Test
    void test3() throws Exception {
        mockMvc.perform(delete("/files/delete-file")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileId\":\"" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет эндпоинт генерации presigned URL для загрузки
     * возвращает 401 без авторизации
     */
    @Test
    void test4() throws Exception {
        mockMvc.perform(post("/files/generate-upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"test.png\",\"fileType\":\"image/png\"}"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет что эндпоинт генерации presigned URL для загрузки
     * возвращает 200 и содержит presignedUrl и key при авторизованном запросе
     */
    @Test
    void test5() throws Exception {
        mockMvc.perform(post("/files/generate-upload-url")
                        .with(jwt().jwt(j -> j.claim("sub", "test")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileName\":\"test.png\",\"fileType\":\"image/png\"}"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presignedUrl").exists())
                .andExpect(jsonPath("$.key").exists());
    }

    /**
     * Проверяет что эндпоинт получения файла по ID
     * возвращает 200 и данные файла при авторизованном запросе владельца
     */
    @Test
    void test6() throws Exception {
        String fileId = "acc78baa-61c4-475c-917c-5d20b552c852";
        String ownerId = "b7383cfc-ebae-4f49-9ca1-31567a62aa32";

        mockMvc.perform(get("/files/file-by-id")
                        .param("fileId", fileId)
                        .with(jwt().jwt(j -> j.claim("sub", ownerId))))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").exists())
                .andExpect(jsonPath("$.size").exists());
    }
}
