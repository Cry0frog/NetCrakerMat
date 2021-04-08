package main;

import main.thread.MultiplierThread;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Main Class
 * @author Valuyskikh Nikita
 * @version 1.1
 */
public class Main {

    /** The number of rows in the first matrix. */
    final static int firstMatrixRows  = 10;
    /** The number of columns in the first matrix. */
    final static int firstMatrixCols  = 10;
    /** The number of rows in the second matrix (must be the same as the number of columns in the first matrix). */
    final static int secondMatrixRows = firstMatrixCols;
    /** The number of columns in the second matrix. */
    final static int secondMatrixCols = 10;

    public static void main(String[] args) {
        final int[][] firstMatrix  = new int[firstMatrixRows][firstMatrixCols];    // Первая (левая) матрица.
        final int[][] secondMatrix = new int[secondMatrixRows][secondMatrixCols];  // Вторая (правая) матрица.

        randomMatrix(firstMatrix);
        randomMatrix(secondMatrix);

        final int[][] resultMatrixMT = multiplyMatrixMT(firstMatrix, secondMatrix, Runtime.getRuntime().availableProcessors());

        final int[][] resultMatrix = multiplyMatrix(firstMatrix, secondMatrix);

        for (int row = 0; row < firstMatrixRows; ++row) {
            for (int col = 0; col < secondMatrixCols; ++col) {
                if (resultMatrixMT[row][col] != resultMatrix[row][col]) {
                    System.out.println("Error in multithreaded calculation!");
                    return;
                }
            }
        }

        printAllMatrix("Matrix.txt", firstMatrix, secondMatrix, resultMatrixMT);
    }

    /** Filling a matrix with random numbers.
     *
     * @param matrix fillable matrix.
     */
    private static void randomMatrix(final int[][] matrix) {
        final Random random = new Random();

        for (int row = 0; row < matrix.length; ++row)
            for (int col = 0; col < matrix[row].length; ++col)
                matrix[row][col] = random.nextInt(100);
    }

    //

    /** Output of the matrix to a file.
     * The values are aligned for better perception.
     *
     * @param fileWriter An object representing the file to write.
     * @param matrix Matrix to output.
     * @throws IOException
     */
    private static void printMatrix(final FileWriter fileWriter, final int[][] matrix) throws IOException {
        boolean hasNegative = false;
        int     maxValue    = 0;

        for (final int[] row : matrix) {
            for (final int element : row) {
                int temp = element;
                if (element < 0) {
                    hasNegative = true;
                    temp = -temp;
                }
                if (temp > maxValue)
                    maxValue = temp;
            }
        }

        int len = Integer.toString(maxValue).length() + 1;
        if (hasNegative)
            ++len;

        final String formatString = "%" + len + "d";

        for (final int[] row : matrix) {
            for (final int element : row)
                fileWriter.write(String.format(formatString, element));

            fileWriter.write("\n");
        }
    }

    /**
     *Output of three matrices to a file. The file will be overwritten.
     *
     * @param fileName The name of the file to be output.
     * @param firstMatrix First matrix.
     * @param secondMatrix Second matrix.
     * @param resultMatrix Result matrix.
     */
    private static void printAllMatrix(final String fileName, final int[][] firstMatrix, final int[][] secondMatrix, final int[][] resultMatrix) {

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

    /** Single-threaded matrix multiplication.
     *
     * @param firstMatrix First matrix.
     * @param secondMatrix Second matrix.
     * @return Result matrix.
     */
    private static int[][] multiplyMatrix(final int[][] firstMatrix, final int[][] secondMatrix) {
        final int rowCount = firstMatrix.length;
        final int colCount = secondMatrix[0].length;
        final int sumLength = secondMatrix.length;
        final int[][] result = new int[rowCount][colCount];

        for (int row = 0; row < rowCount; ++row) {
            for (int col = 0; col < colCount; ++col) {
                int sum = 0;
                for (int i = 0; i < sumLength; ++i)
                    sum += firstMatrix[row][i] * secondMatrix[i][col];
                result[row][col] = sum;
            }
        }
        return result;
    }

    /** Multithreaded matrix multiplication.
     *
     * @param firstMatrix First (left) matrix.
     * @param secondMatrix Second (right) matrix.
     * @param threadCount Number of threads.
     * @return Result matrix.
     */
    private static int[][] multiplyMatrixMT(final int[][] firstMatrix, final int[][] secondMatrix, int threadCount) {

        assert threadCount > 0;

        final int rowCount = firstMatrix.length;
        final int colCount = secondMatrix[0].length;
        final int[][] result = new int[rowCount][colCount];

        final int cellsForThread = (rowCount * colCount) / threadCount;
        int firstIndex = 0;
        final MultiplierThread[] multiplierThreads = new MultiplierThread[threadCount];

        for (int threadIndex = threadCount - 1; threadIndex >= 0; --threadIndex) {
            int lastIndex = firstIndex + cellsForThread;
            if (threadIndex == 0) {
                lastIndex = rowCount * colCount;
            }
            multiplierThreads[threadIndex] = new MultiplierThread(firstMatrix, secondMatrix, result, firstIndex, lastIndex);
            multiplierThreads[threadIndex].start();
            firstIndex = lastIndex;
        }

        try {
            for (final MultiplierThread multiplierThread : multiplierThreads)
                multiplierThread.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

}

