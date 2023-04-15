package ru.yandex.yandexlavka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@EntityScan(basePackages = {"ru.yandex.yandexlavka.model.entity"})
public class YandexLavkaApplication {

    public static void main(String[] args) {
        SpringApplication.run(YandexLavkaApplication.class, args);
    }

}
