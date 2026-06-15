package com.glebkrylatov.filesharingapi.servicies;

import org.springframework.stereotype.Service;

//Сервис для работы с S3 хранилищем (Minio)
@Service
public class StorageService {
    //TODO:
    //Написание создания presigned URL для операций с файлами

    //Возвращает ссылку для загрузки файла напрямую в Minio хранилище. Время жизни ссылки 5 минут
    public String generateUploadUrl(String key, String contentType) {
        //TODO: НАПИСАНИЕ СОЗДАНИЯ ССЫЛКИ ДЛЯ ЗАГРУЗКИ ФАЙЛА НАПРЯМУЮ В MINIO ХРАНИЛИЩЕ
        return "test";
    }

    //Возвращает ссылку для загрузки файла напрямую в Minio хранилище. Время жизни 5 минут
    public String generateDownloadUrl(String key) {
        //TODO: НАПИСАНИЕ СОЗДАНИЯ ССЫЛКИ ДЛЯ ЗАГРУЗКИ ФАЙЛА НАПРЯМУЮ С MINIO
        return "test";
    }

    //
    public boolean deleteFileFromStorage(String key) {
        //TODO: НАПИСАНИЕ УДАЛЕНИЯ ФАЙЛА В MINIO
        return true;
    }
}
