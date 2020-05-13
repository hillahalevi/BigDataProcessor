package com.zoomInfo;

import com.zoomInfo.model.Entry;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.zoomInfo.chuncks.Constants.*;
import static com.zoomInfo.chuncks.Constants.NUM_OF_ENTRIES;

@Component
public class FileUtils {


    private static final List<String> FIRST_NAMES = Arrays.asList("Imri",
            "Jo",
            "Yossi", "Maor'k", "Ben", "Hilla", "ZooM'INFO");
    private static final List<String> LAST_NAMES = Arrays.asList("Halevi",
            "Ben-David",
            "Menashe", "Cohen", "Mazig", "Lenon", "Scorpions");


    public static void preRun() throws Exception {
        //delete old output file if exist
        Path fileToDeletePath = Paths.get(OUTPUT_DATA_CSV);
        Files.deleteIfExists(fileToDeletePath);

        //create entries file
        createEateriesFile(TEST_FILE, NUM_OF_ENTRIES);
    }

    private static void createEateriesFile(String path, int numOfEntries) throws IOException, CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        //create entries
        ArrayList<Entry> entries = createEntries(numOfEntries);

        //set appropriate mapping
        ColumnPositionMappingStrategy<Entry> mappingStrategy =
                new ColumnPositionMappingStrategy<>();
        mappingStrategy.setType(Entry.class);
        mappingStrategy.setColumnMapping(ENTRY_PROPERTIES_COLUMNS);

        //write in csv format
        Writer writer = new FileWriter(path);
        StatefulBeanToCsv<Entry> beanToCsv = new StatefulBeanToCsvBuilder<Entry>(writer).withMappingStrategy(mappingStrategy).build();
        beanToCsv.write(entries);
        writer.close();
    }


    private static ArrayList<Entry> createEntries(int numOfEntries) {
        ArrayList<Entry> entries = new ArrayList<>();
        Random random = new Random();
        IntStream.rangeClosed(0, numOfEntries).forEach(i -> {
            String firstName = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
            String lastName = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
            Entry entry = new Entry(Integer.toString(i), firstName, lastName, random.nextInt(100));
            entries.add(entry);
        });
        return entries;


    }


}
