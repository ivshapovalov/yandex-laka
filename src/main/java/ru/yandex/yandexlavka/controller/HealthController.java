package ru.yandex.yandexlavka.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class HealthController {

//    private final RateLimiter rateLimiter;

//    public HealthController(    @Autowired
//                                        RateLimiterRegistry rateLimiterRegistry) {
//        this.rateLimiter = rateLimiterRegistry.rateLimiter("rateLimiterApi");
//    }

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/ping",
            produces = {"text/plain"}
    )
    @RateLimiter(name = "healthRateLimiter")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("pong");
    }


    @RequestMapping(
            method = RequestMethod.GET,
            value = "/test",
            produces = {"application/json"}
    )
    @RateLimiter(name = "healthRateLimiter")
    public ResponseEntity<List<Integer>> test() {
        return ResponseEntity.ok(List.of(8,
                6,
                -2,
                2,
                4,
                17,
                256,
                1024,
                -17,
                -19).stream().collect(Collectors.toList()));
    }


}
