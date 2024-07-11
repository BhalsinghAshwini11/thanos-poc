package com.prometheus.thanos.exporter.poc.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.Random;

@RestController
public class DemoController {
    private static final Logger logger = LoggerFactory.getLogger(DemoController.class);
    private final HealthService healthService;

    public DemoController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping("/ping")
    public String ping() {
        return "Pong";
    }

    @GetMapping("/random")
    public String index(@RequestParam("user") Optional<String> user) {
        int result = this.getRandomNumber();
        if (user.isPresent()) {
            logger.info("{} is requesting random number: {}", user.get(), result);
        } else {
            logger.info("Unknown user requesting random number: {}", result);
        }
        return Integer.toString(result);
    }

    public int getRandomNumber() {
        return new Random().nextInt(0, 100);
    }

    @GetMapping("/health")
    public String health() {
        return healthService.health().toString();
    }
}
