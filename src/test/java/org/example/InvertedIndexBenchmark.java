package org.example;
import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Класс InvertedIndexBenchmark содержит бенчмарки для измерения производительности InvertedIndex.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class InvertedIndexBenchmark {

    private InvertedIndex invertedIndex;
    private List<String> documents;
    private List<String> queryTermsAnd;
    private List<String> queryTermsOr;
    private final String directoryPath = "src/main/resources";

    @Setup
    public void setUp() throws IOException {
        invertedIndex = new InvertedIndex();

        // Загрузка документов
        documents = DocumentLoader.loadDocuments(directoryPath);
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

        // Определение поисковых запросов
        queryTermsAnd = Arrays.asList("people", "publish", "american", "books", "black");
        queryTermsOr = Arrays.asList("people", "publish", "american", "books", "black");
    }

    /**
     * Бенчмарк для булевого поиска с операцией AND.
     *
     * @return Множество документов, соответствующих запросу.
     * @throws IOException При ошибке декомпрессии.
     */
    @Benchmark
    public Set<Integer> benchmarkBooleanAndSearch() throws IOException {
        return invertedIndex.booleanAndSearch(queryTermsAnd);
    }

    /**
     * Бенчмарк для булевого поиска с операцией OR.
     *
     * @return Множество документов, соответствующих запросу.
     * @throws IOException При ошибке декомпрессии.
     */
    @Benchmark
    public Set<Integer> benchmarkBooleanOrSearch() throws IOException {
        return invertedIndex.booleanOrSearch(queryTermsOr);
    }


    /**
     * Бенчмарк для сжатия индекса.
     *
     * @throws IOException При ошибке сжатия.
     */
    @Benchmark
    public void benchmarkCompress() throws IOException {
        invertedIndex.compress();
    }
}
