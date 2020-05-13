package com.zoomInfo.chuncks;

/**
 * Big data processor!
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.zoomInfo.FileUtils.preRun;


@SpringBootApplication
@EnableAutoConfiguration
public class BatchRunner {


    public static void main(String[] args) throws Exception {
//        preRun();
//        System.out.println("properties  :");
//        String properties = String.format("num of entries: %s , chunk size: %s , throttle limit : %s ", NUM_OF_ENTRIES, CHUNK_SIZE, THROTTLE_LIMIT);
//        System.out.println(properties);
        SpringApplication.run(BatchRunner.class, args);
    }

}
