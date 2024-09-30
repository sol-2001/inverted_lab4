package org.example;

import java.io.IOException;
import java.util.*;

/**
 * Класс Main демонстрирует использование InvertedIndex.
 */
public class Main {
    public static void main(String[] args) {
        InvertedIndex invertedIndex = new InvertedIndex();

        String directoryPath = "src/main/resources";

        try {
            // Загрузка документов
            List<String> documents = DocumentLoader.loadDocuments(directoryPath);
            System.out.println("Загружено документов: " + documents.size());

            // Добавление документов в индекс
            int docId = 1;
            for (String content : documents) {
                invertedIndex.addDocument(docId++, content);
            }
            System.out.println("Добавлено документов в индекс.");

            // Сжатие индекса
            invertedIndex.compress();
            System.out.println("Индекс сжат.");

            // Пример булевого поиска (AND)
            List<String> queryAnd = Arrays.asList("people", "publish", "american", "books", "black");
            Set<Integer> resultsAnd = invertedIndex.booleanAndSearch(queryAnd);
            System.out.println("AND Search Results for " + queryAnd + ": " + resultsAnd);

            // Пример булевого поиска (OR)
            List<String> queryOr = Arrays.asList("people", "publish", "american", "books", "black");
            Set<Integer> resultsOr = invertedIndex.booleanOrSearch(queryOr);
            System.out.println("OR Search Results for " + queryOr + ": " + resultsOr);

            // Сохранение индекса на диск
            String filePath = "invertedIndex.ser";
            invertedIndex.save(filePath);
            System.out.println("Индекс сохранён в " + filePath);

            // Загрузка индекса с диска
            InvertedIndex loadedIndex = InvertedIndex.load(filePath);
            System.out.println("Индекс загружен из " + filePath);

            // Поиск после загрузки
            Set<Integer> loadedResultsAnd = loadedIndex.booleanAndSearch(queryAnd);
            System.out.println("Loaded AND Search Results for " + queryAnd + ": " + loadedResultsAnd);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}