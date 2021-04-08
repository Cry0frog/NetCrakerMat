package main;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main
{
    /** Заполнение матрицы случайными числами.
     *
     * @param matrix Заполняемая матрица.
     */
    private static void randomMatrix(final int[][] matrix)
    {
        final Random random = new Random();  // Генератор случайных чисел.

        for (int row = 0; row < matrix.length; ++row)           // Цикл по строкам матрицы.
            for (int col = 0; col < matrix[row].length; ++col)  // Цикл по столбцам матрицы.
                matrix[row][col] = random.nextInt(100);         // Случайное число от 0 до 100.
    }

    //

    /** Вывод матрицы в файл.
     * Производится выравнивание значений для лучшего восприятия.
     *
     * @param fileWriter Объект, представляющий собой файл для записи.
     * @param matrix Выводимая матрица.
     * @throws IOException
     */
    private static void printMatrix(final FileWriter fileWriter,
                                    final int[][] matrix) throws IOException
    {
        boolean hasNegative = false;  // Признак наличия в матрице отрицательных чисел.
        int     maxValue    = 0;      // Максимальное по модулю число в матрице.

        // Вычисляем максимальное по модулю число в матрице и проверяем на наличие отрицательных чисел.
        for (final int[] row : matrix) {  // Цикл по строкам матрицы.
            for (final int element : row) {  // Цикл по столбцам матрицы.
                int temp = element;
                if (element < 0) {
                    hasNegative = true;
                    temp = -temp;
                }
                if (temp > maxValue)
                    maxValue = temp;
            }
        }

        // Вычисление длины позиции под число.
        int len = Integer.toString(maxValue).length() + 1;  // Одно знакоместо под разделитель (пробел).
        if (hasNegative)
            ++len;  // Если есть отрицательные, добавляем знакоместо под минус.

        // Построение строки формата.
        final String formatString = "%" + len + "d";

        // Вывод элементов матрицы в файл.
        for (final int[] row : matrix) {  // Цикл по строкам матрицы.
            for (final int element : row)  // Цикл по столбцам матрицы.
                fileWriter.write(String.format(formatString, element));

            fileWriter.write("\n");  // Разделяем строки матрицы переводом строки.
        }
    }

    /**
     * Вывод трёх матриц в файл. Файл будет перезаписан.
     *
     * @param fileName     Имя файла для вывода.
     * @param firstMatrix  Первая матрица.
     * @param secondMatrix Вторая матрица.
     * @param resultMatrix Результирующая матрица.
     */
    private static void printAllMatrix(final String fileName,
                                       final int[][] firstMatrix,
                                       final int[][] secondMatrix,
                                       final int[][] resultMatrix)
    {
        try (final FileWriter fileWriter = new FileWriter(fileName, false)) {
            fileWriter.write("First matrix:\n");
            printMatrix(fileWriter, firstMatrix);

            fileWriter.write("\nSecond matrix:\n");
            printMatrix(fileWriter, secondMatrix);

            fileWriter.write("\nResult matrix:\n");
            printMatrix(fileWriter, resultMatrix);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Однопоточное умножение матриц.
     *
     * @param firstMatrix  Первая матрица.
     * @param secondMatrix Вторая матрица.
     * @return Результирующая матрица.
     */
    private static int[][] multiplyMatrix(final int[][] firstMatrix,
                                          final int[][] secondMatrix)
    {
        final int rowCount = firstMatrix.length;             // Число строк результирующей матрицы.
        final int colCount = secondMatrix[0].length;         // Число столбцов результирующей матрицы.
        final int sumLength = secondMatrix.length;           // Число членов суммы при вычислении значения ячейки.
        final int[][] result = new int[rowCount][colCount];  // Результирующая матрица.

        for (int row = 0; row < rowCount; ++row) {  // Цикл по строкам матрицы.
            for (int col = 0; col < colCount; ++col) {  // Цикл по столбцам матрицы.
                int sum = 0;
                for (int i = 0; i < sumLength; ++i)
                    sum += firstMatrix[row][i] * secondMatrix[i][col];
                result[row][col] = sum;
            }
        }

        return result;
    }

    /** Многопоточное умножение матриц.
     *
     * @param firstMatrix  Первая (левая) матрица.
     * @param secondMatrix Вторая (правая) матрица.
     * @param threadCount Число потоков.
     * @return Результирующая матрица.
     */
    private static int[][] multiplyMatrixMT(final int[][] firstMatrix,
                                            final int[][] secondMatrix,
                                            int threadCount)
    {
        assert threadCount > 0;

        final int rowCount = firstMatrix.length;             // Число строк результирующей матрицы.
        final int colCount = secondMatrix[0].length;         // Число столбцов результирующей матрицы.
        final int[][] result = new int[rowCount][colCount];  // Результирующая матрица.

        final int cellsForThread = (rowCount * colCount) / threadCount;  // Число вычисляемых ячеек на поток.
        int firstIndex = 0;  // Индекс первой вычисляемой ячейки.
        final MultiplierThread[] multiplierThreads = new MultiplierThread[threadCount];  // Массив потоков.

        // Создание и запуск потоков.
        for (int threadIndex = threadCount - 1; threadIndex >= 0; --threadIndex) {
            int lastIndex = firstIndex + cellsForThread;  // Индекс последней вычисляемой ячейки.
            if (threadIndex == 0) {
                /* Один из потоков должен будет вычислить не только свой блок ячеек,
                   но и остаток, если число ячеек не делится нацело на число потоков. */
                lastIndex = rowCount * colCount;
            }
            multiplierThreads[threadIndex] = new MultiplierThread(firstMatrix, secondMatrix, result, firstIndex, lastIndex);
            multiplierThreads[threadIndex].start();
            firstIndex = lastIndex;
        }

        // Ожидание завершения потоков.
        try {
            for (final MultiplierThread multiplierThread : multiplierThreads)
                multiplierThread.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    /** Число строк первой матрицы. */
    final static int FIRST_MATRIX_ROWS  = 1000;
    /** Число столбцов первой матрицы. */
    final static int FIRST_MATRIX_COLS  = 1000;
    /** Число строк второй матрицы (должно совпадать с числом столбцов первой матрицы). */
    final static int SECOND_MATRIX_ROWS = FIRST_MATRIX_COLS;
    /** Число столбцов второй матрицы. */
    final static int SECOND_MATRIX_COLS = 1000;

    public static void main(String[] args)
    {
        final int[][] firstMatrix  = new int[FIRST_MATRIX_ROWS][FIRST_MATRIX_COLS];    // Первая (левая) матрица.
        final int[][] secondMatrix = new int[SECOND_MATRIX_ROWS][SECOND_MATRIX_COLS];  // Вторая (правая) матрица.

        randomMatrix(firstMatrix);
        randomMatrix(secondMatrix);

        final int[][] resultMatrixMT = multiplyMatrixMT(firstMatrix, secondMatrix, Runtime.getRuntime().availableProcessors());

        // Проверка многопоточных вычислений с помощью однопоточных.
        final int[][] resultMatrix = multiplyMatrix(firstMatrix, secondMatrix);

        for (int row = 0; row < FIRST_MATRIX_ROWS; ++row) {
            for (int col = 0; col < SECOND_MATRIX_COLS; ++col) {
                if (resultMatrixMT[row][col] != resultMatrix[row][col]) {
                    System.out.println("Error in multithreaded calculation!");
                    return;
                }
            }
        }

        printAllMatrix("Matrix.txt", firstMatrix, secondMatrix, resultMatrixMT);
    }
}

/** Поток-вычислитель группы ячеек матрицы. */
class MultiplierThread extends Thread
{
    /** Первая (левая) матрица. */
    private final int[][] firstMatrix;
    /** Вторая (правая) матрица. */
    private final int[][] secondMatrix;
    /** Результирующая матрица. */
    private final int[][] resultMatrix;
    /** Начальный индекс. */
    private final int firstIndex;
    /** Конечный индекс. */
    private final int lastIndex;
    /** Число членов суммы при вычислении значения ячейки. */
    private final int sumLength;

    /**
     * @param firstMatrix  Первая (левая) матрица.
     * @param secondMatrix Вторая (правая) матрица.
     * @param resultMatrix Результирующая матрица.
     * @param firstIndex   Начальный индекс (ячейка с этим индексом вычисляется).
     * @param lastIndex    Конечный индекс (ячейка с этим индексом не вычисляется).
     */
    public MultiplierThread(final int[][] firstMatrix,
                            final int[][] secondMatrix,
                            final int[][] resultMatrix,
                            final int firstIndex,
                            final int lastIndex)
    {
        this.firstMatrix  = firstMatrix;
        this.secondMatrix = secondMatrix;
        this.resultMatrix = resultMatrix;
        this.firstIndex   = firstIndex;
        this.lastIndex    = lastIndex;

        sumLength = secondMatrix.length;
    }

    /**Вычисление значения в одной ячейке.
     *
     * @param row Номер строки ячейки.
     * @param col Номер столбца ячейки.
     */
    private void calcValue(final int row, final int col)
    {
        int sum = 0;
        for (int i = 0; i < sumLength; ++i)
            sum += firstMatrix[row][i] * secondMatrix[i][col];
        resultMatrix[row][col] = sum;
    }

    /** Рабочая функция потока. */
    @Override
    public void run()
    {
        System.out.println("Thread " + getName() + " started. Calculating cells from " + firstIndex + " to " + lastIndex + "...");

        final int colCount = secondMatrix[0].length;  // Число столбцов результирующей матрицы.
        for (int index = firstIndex; index < lastIndex; ++index)
            calcValue(index / colCount, index % colCount);

        System.out.println("Thread " + getName() + " finished.");
    }
}