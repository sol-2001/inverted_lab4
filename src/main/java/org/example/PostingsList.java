package org.example;

import java.util.*;
import java.io.*;

/**
 * Класс PostingsList реализует сжатие и декомпрессию постинг-листа с использованием pForDelta.
 */
public class PostingsList implements Serializable {
    private static final long serialVersionUID = 1L;

    // Список идентификаторов документов
    private List<Integer> documentIds;

    // Сжатые данные постинг-листа
    private byte[] compressedData;

    public PostingsList() {
        this.documentIds = new ArrayList<>();
    }

    /**
     * Добавляет идентификатор документа в постинг-лист.
     *
     * @param docId Идентификатор документа
     */
    public void add(int docId) {
        documentIds.add(docId);
    }

    /**
     * Сжимает постинг-лист с использованием pForDelta.
     *
     * @throws IOException При ошибке ввода-вывода
     */
    public void compress() throws IOException {
        if (documentIds.isEmpty()) return;

        // Вычисление гэпов
        List<Integer> gaps = new ArrayList<>();
        gaps.add(documentIds.get(0));
        for (int i = 1; i < documentIds.size(); i++) {
            gaps.add(documentIds.get(i) - documentIds.get(i - 1));
        }

        // Разбиение на блоки
        int blockSize = 128;
        List<List<Integer>> blocks = new ArrayList<>();
        for (int i = 0; i < gaps.size(); i += blockSize) {
            blocks.add(gaps.subList(i, Math.min(i + blockSize, gaps.size())));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        for (List<Integer> block : blocks) {
            int max = Collections.max(block);
            int bitWidth = 32 - Integer.numberOfLeadingZeros(max);
            if (bitWidth == 0) bitWidth = 1;

            // Запись bitWidth
            dos.writeInt(bitWidth);

            // Кодирование гэпов
            BitSet bitSet = new BitSet(block.size() * bitWidth);
            int bitIndex = 0;
            for (int gap : block) {
                for (int b = bitWidth - 1; b >= 0; b--) {
                    if ((gap & (1 << b)) != 0) {
                        bitSet.set(bitIndex);
                    }
                    bitIndex++;
                }
            }

            // Преобразование BitSet в байты
            byte[] bytes = bitSet.toByteArray();
            dos.writeInt(bytes.length);
            dos.write(bytes);
        }

        dos.flush();
        compressedData = baos.toByteArray();
        dos.close();
        baos.close();
    }

    /**
     * Декомпрессирует постинг-лист.
     *
     * @return Список идентификаторов документов
     * @throws IOException При ошибке ввода-вывода
     */
    public List<Integer> decompress() throws IOException {
        List<Integer> decompressed = new ArrayList<>();
        if (compressedData == null) return decompressed;

        ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
        DataInputStream dis = new DataInputStream(bais);

        while (dis.available() > 0) {
            // Чтение bitWidth
            int bitWidth = dis.readInt();

            // Чтение длины блока
            int byteLength = dis.readInt();

            // Чтение сжатых данных
            byte[] bytes = new byte[byteLength];
            dis.readFully(bytes);
            BitSet bitSet = BitSet.valueOf(bytes);

            // Декодирование гэпов
            int numGaps = (byteLength * 8) / bitWidth;
            for (int i = 0; i < numGaps; i++) {
                int gap = 0;
                for (int b = 0; b < bitWidth; b++) {
                    if (bitSet.get(i * bitWidth + b)) {
                        gap |= (1 << (bitWidth - 1 - b));
                    }
                }
                decompressed.add(gap);
            }
        }

        dis.close();
        bais.close();

        // Восстановление documentIds из гэпов
        List<Integer> docIds = new ArrayList<>();
        if (decompressed.isEmpty()) return docIds;

        docIds.add(decompressed.get(0));
        for (int i = 1; i < decompressed.size(); i++) {
            docIds.add(docIds.get(i - 1) + decompressed.get(i));
        }

        return docIds;
    }

    /**
     * Получает сжатые данные постинг-листа.
     *
     * @return Массив байт сжатых данных
     */
    public byte[] getCompressedData() {
        return compressedData;
    }

    /**
     * Устанавливает сжатые данные постинг-листа.
     *
     * @param data Массив байт сжатых данных
     */
    public void setCompressedData(byte[] data) {
        this.compressedData = data;
    }

    /**
     * Получает список идентификаторов документов.
     *
     * @return Список идентификаторов документов
     */
    public List<Integer> getDocumentIds() {
        return documentIds;
    }
}
