package main.thread;

/**
 * A thread-calculator of a group of matrix cells..
 * @author Valuyskikh Nikita
 * @version 1.1
 */
public class MultiplierThread extends Thread {

    /**
     * First (left) matrix.
     */
    private final int[][] firstMatrix;
    /**
     * Second (right) matrix.
     */
    private final int[][] secondMatrix;
    /**
     * Result matrix.
     */
    private final int[][] resultMatrix;
    /**
     * Starting index.
     */
    private final int firstIndex;
    /**
     * End index.
     */
    private final int lastIndex;
    /**
     * The number of sum members when calculating the cell value.
     */
    private final int sumLength;

    /**
     * @param firstMatrix  First (left) matrix.
     * @param secondMatrix Second (right) matrix.
     * @param resultMatrix Result matrix.
     * @param firstIndex   Starting index (the cell with this index is calculated).
     * @param lastIndex    End index (the cell with this index is not calculated).
     */
    public MultiplierThread(final int[][] firstMatrix, final int[][] secondMatrix, final int[][] resultMatrix, final int firstIndex, final int lastIndex) {

        this.firstMatrix = firstMatrix;
        this.secondMatrix = secondMatrix;
        this.resultMatrix = resultMatrix;
        this.firstIndex = firstIndex;
        this.lastIndex = lastIndex;

        sumLength = secondMatrix.length;
    }

    /**
     * Calculate the value in one cell.
     *
     * @param row The row number of the cell.
     * @param col Column number of the cell.
     */
    private void calcValue(final int row, final int col) {
        int sum = 0;
        for (int i = 0; i < sumLength; ++i)
            sum += firstMatrix[row][i] * secondMatrix[i][col];
        resultMatrix[row][col] = sum;
    }

    /**
     * Work function of the thread.
     */
    @Override
    public void run() {
        System.out.println("Thread " + getName() + " started. Calculating cells from " + firstIndex + " to " + lastIndex + "...");

        final int colCount = secondMatrix[0].length;
        for (int index = firstIndex; index < lastIndex; ++index)
            calcValue(index / colCount, index % colCount);

        System.out.println("Thread " + getName() + " finished.");
    }
}
