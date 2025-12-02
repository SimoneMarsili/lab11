package it.unibo.oop.workers02;

import java.util.stream.IntStream;

/**
 * A multithreadded implementation of SumMatrix.
 */
@SuppressWarnings("CPD-START")
public final class MultiThreadedSumMatrix implements SumMatrix {

    private final int nthread;

    /**
     * @param nthread
     *            no. of thread performing the sum.
     */
    public MultiThreadedSumMatrix(final int nthread) {
        this.nthread = nthread;
    }

    @Override
    public double sum(final double[][] matrix) {
        final int size = matrix.length % nthread + matrix.length / nthread;
        return IntStream
                .iterate(0, start -> start + size)
                .limit(nthread)
                .mapToObj(start -> new Worker(matrix, start, size))
                // Start them
                .peek(Thread::start)
                // Join them
                .peek(MultiThreadedSumMatrix::joinUninterruptibly)
                // Get their result and sum
                .mapToDouble(Worker::getResult)
                .sum();
    }

    @SuppressWarnings("PMD.AvoidPrintStackTrace")
    private static void joinUninterruptibly(final Thread target) {
        var joined = false;
        while (!joined) {
            try {
                target.join();
                joined = true;
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startrow;
        private final int nrows;
        private double res;

        /**
         * Build a new worker.
         *
         * @param matrix
         *            the matrix to sum
         * @param startrow
         *            the initial row for this worker
         * @param nrows
         *            the no. of rows to sum up for this worker
         */
        Worker(final double[][] matrix, final int startrow, final int nrows) {
            super();
            this.matrix = matrix; // NOPMD : avoiding to copy an entire big matrix
            this.startrow = startrow;
            this.nrows = nrows;
        }

        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public synchronized void run() {
            System.out.println("Working from row " + startrow + " to row " + (startrow + nrows - 1));
            for (int i = startrow; i < matrix.length && i < startrow + nrows; i++) {
                for (int j = 0; j < matrix.length; j++) {
                    this.res += this.matrix[i][j];
                }
            }
            System.out.println(res);
        }

        /**
         * Returns the result of summing up the integers within the list.
         *
         * @return the sum of every element in the array
         */
        public synchronized double getResult() {
            return this.res;
        }

    }

}
