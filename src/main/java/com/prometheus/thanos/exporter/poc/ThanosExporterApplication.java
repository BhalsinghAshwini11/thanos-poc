package com.prometheus.thanos.exporter.poc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ThanosExporterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThanosExporterApplication.class, args);
    }

}
