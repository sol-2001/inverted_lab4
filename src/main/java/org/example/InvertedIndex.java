package org.example;

import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * Класс InvertedIndex реализует обратный индекс с поддержкой булевого поиска и сжатыми постинг-листами.
 */
public class InvertedIndex implements Serializable {
    private static final long serialVersionUID = 1L;

    // Словарь термов и соответствующих постинг-листов
    private Map<String, PostingsList> index;

    // Флаг, указывающий, сжат ли индекс
    private boolean isCompressed;

    public InvertedIndex() {
        this.index = new HashMap<>();
        this.isCompressed = false;
    }

    /**
     * Добавляет документ в индекс.
     *
     * @param docId  Идентификатор документа
     * @param content Содержимое документа
     */
    public void addDocument(int docId, String content) {
        String[] terms = tokenize(content);
        for (String term : terms) {
            PostingsList postings = index.getOrDefault(term, new PostingsList());
            postings.add(docId);
            index.put(term, postings);
        }
    }

    /**
     * Токенизирует текст: преобразует в нижний регистр и разбивает по пробелам.
     *
     * @param text Текст для токенизации
     * @return Массив термов
     */
    private String[] tokenize(String text) {
        return text.toLowerCase().split("\\s+");
    }

    /**
     * Сжимает все постинг-листы в индексе.
     *
     * @throws IOException При ошибке ввода-вывода
     */
    public void compress() throws IOException {
        for (Map.Entry<String, PostingsList> entry : index.entrySet()) {
            entry.getValue().compress();
        }
        isCompressed = true;
    }

    /**
     * Получает постинг-лист для заданного терма.
     *
     * @param term Термин
     * @return Список идентификаторов документов, содержащих терм
     * @throws IOException При ошибке декомпрессии
     */
    public List<Integer> getPostings(String term) throws IOException {
        PostingsList postings = index.get(term);
        if (postings == null) return Collections.emptyList();
        if (isCompressed) {
            return postings.decompress();
        } else {
            return postings.getDocumentIds();
        }
    }

    /**
     * Булевый поиск с операцией AND.
     *
     * @param terms Список термов для поиска
     * @return Множество идентификаторов документов, содержащих все термы
     * @throws IOException При ошибке декомпрессии
     */
    public Set<Integer> booleanAndSearch(List<String> terms) throws IOException {
        Set<Integer> result = null;
        for (String term : terms) {
            List<Integer> postings = getPostings(term);
            if (result == null) {
                result = new HashSet<>(postings);
            } else {
                result.retainAll(postings);
            }
            if (result.isEmpty()) break;
        }
        return result != null ? result : Collections.emptySet();
    }

    /**
     * Булевый поиск с операцией OR.
     *
     * @param terms Список термов для поиска
     * @return Множество идентификаторов документов, содержащих хотя бы один терм
     * @throws IOException При ошибке декомпрессии
     */
    public Set<Integer> booleanOrSearch(List<String> terms) throws IOException {
        Set<Integer> result = new HashSet<>();
        for (String term : terms) {
            List<Integer> postings = getPostings(term);
            result.addAll(postings);
        }
        return result;
    }

    /**
     * Сохраняет индекс на диск.
     *
     * @param filePath Путь к файлу для сохранения
     * @throws IOException При ошибке ввода-вывода
     */
    public void save(String filePath) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(filePath)))) {
            oos.writeObject(this);
        }
    }

    /**
     * Загружает индекс с диска.
     *
     * @param filePath Путь к файлу для загрузки
     * @return Загруженный объект InvertedIndex
     * @throws IOException            При ошибке ввода-вывода
     * @throws ClassNotFoundException При ошибке десериализации
     */
    public static InvertedIndex load(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(Paths.get(filePath)))) {
            return (InvertedIndex) ois.readObject();
        }
    }

    /**
     * Получает количество термов в индексе.
     *
     * @return Количество термов
     */
    public int getTermCount() {
        return index.size();
    }
}
