package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DocumentLoader {

    /**
     * Загружает все текстовые файлы из указанной директории и её поддиректорий.
     *
     * @param directoryPath Путь к директории с документами.
     * @return Список содержимого всех документов.
     * @throws IOException При ошибке чтения файлов.
     */
    public static List<String> loadDocuments(String directoryPath) throws IOException {
        List<String> documents = new ArrayList<>();

        // Используем Files.walk для рекурсивного обхода директорий
        Files.walk(Paths.get(directoryPath))
                .filter(Files::isRegularFile) // Отбираем только файлы
                .filter(path -> path.toString().endsWith(".txt")) // Отбираем только .txt
                .forEach(path -> {
                    try {
                        String content = Files.readString(path);
                        documents.add(content);
                    } catch (IOException e) {
                        System.err.println("Ошибка чтения файла: " + path);
                        e.printStackTrace();
                    }
                });

        return documents;
    }
}