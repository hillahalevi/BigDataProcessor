package com.zoomInfo.producerConsumer;

import com.zoomInfo.model.Entry;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zoomInfo.chuncks.Constants.*;

/**
 * Producer Consumer paradigm
 * q1 queue to hold lines from file with fixed capacity
 * q2 queue to hold eateries to result file with fixed capacity
 * the producer thread  reads constantly lines and adds them to q1
 * the consumer threads take input from q1 - process them and if needed adds to q2
 * whenever q2 reached max capacity - write all content to file and then clear q2
 * results : 1000000 entries processed in 15778ms  (chunk size -1000, pool limit = 20)
 * results : 100 entries processed in 36ms  (chunk size -20, pool limit = 10)
 */
public class ProducerConsumer implements Runnable {


    private final static BlockingQueue<String> linesReadQueue = new ArrayBlockingQueue<>(CHUNK_SIZE);
    private final static BlockingQueue<Entry> linesWriteQueue = new ArrayBlockingQueue<>(CHUNK_SIZE);

    private boolean isConsumer;
    private static boolean producerIsDone = false;


    public ProducerConsumer(boolean consumer) {
        this.isConsumer = consumer;
    }

    @Override
    public void run() {
        if (isConsumer) {
            consume();
        } else {
            readFile();
        }
    }

    private void consume() {
        try {
            while (!producerIsDone || (producerIsDone && !linesReadQueue.isEmpty())) {
                String lineToProcess = linesReadQueue.take();
                process(lineToProcess);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread().getName() + " consumer is done");
    }

    private void process(String lineToProcess) throws Exception {
        String[] items = lineToProcess.split(",");
        Entry entry = new Entry(items[0], items[1], items[2], Integer.parseInt(items[3].replaceAll("\"", "")));
        System.out.println(entry);
        if (entry.getFirstName().contains("'")) {
            if (linesWriteQueue.remainingCapacity() == 0) {
                // whenever reaches max capacity - write
                writeToFile();
            }
            linesWriteQueue.put(entry);
        }
    }

    private void readFile() {
        Path file = Paths.get(TEST_FILE);
        try {
            Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8);
            for (String line : (Iterable<String>) lines::iterator) {
                linesReadQueue.put(line); //blocked if reaches its capacity, until consumer consumes
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        producerIsDone = true; // signal consumer
        System.out.println(Thread.currentThread().getName() + " producer is done");
    }

    private synchronized void writeToFile() throws Exception {
        ArrayList<Entry> entries = new ArrayList<>();
        linesWriteQueue.drainTo(entries);
        ArrayList<String> ids = entries.stream().map(Entry::getId).collect(Collectors.toCollection(ArrayList::new));
        FileWriter fw = new FileWriter(OUTPUT_DATA_CSV, true);
        for (String id : ids) {
            fw.write(id);
            fw.write("\n");
        }
        linesWriteQueue.clear();
        fw.close();
    }

}