package com.zoomInfo.producerConsumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.zoomInfo.FileUtils.preRun;
import static com.zoomInfo.chuncks.Constants.THROTTLE_LIMIT;

public class ProducerConsumerRunner {

    public static void main(String[] args) throws Exception {
        preRun();
        long startTime = System.nanoTime();

        ExecutorService readerPool = Executors.newFixedThreadPool(1);
        readerPool.submit(new ProducerConsumer(false)); // run method is  called

        // create a pool of consumer threads to parse the lines read
        ExecutorService processorPool = Executors.newFixedThreadPool(THROTTLE_LIMIT);
        for (int i = 0; i < THROTTLE_LIMIT; i++) {
            processorPool.submit(new ProducerConsumer(true)); // run method is  called
        }

        readerPool.shutdown();
        processorPool.shutdown();


        try {
            processorPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            long endTime = System.nanoTime();
            long elapsedTimeInMillis = TimeUnit.MILLISECONDS.convert((endTime - startTime), TimeUnit.NANOSECONDS);
            System.out.println("Total elapsed time: " + elapsedTimeInMillis + " ms");
        } catch (InterruptedException e) {

        }
    }


}

