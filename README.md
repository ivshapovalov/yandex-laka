# ШБР Яндекс Лето 2023. Продуктовая задача в рамках отбора в ШБР.

### Стек технологий:
* Spring boot 3
* PostgreSQL 15.2
* Gradle 8
* Docker
* jSprit 1.8
* resilience4j
* liquibase
--------------------------------------
Исходный текст задания можно посмотреть [здесь](https://github.com/ivshapovalov/yandex-lavka/tree/main/docs/task/README.md)

Спецификацию к API можно посмотреть [здесь](https://github.com/ivshapovalov/yandex-lavka/tree/main/docs/task/openapi.json)

--------------------------------------
## Выполнены задания:

1) REST API сервиса — Задание 1;
2) Расчет рейтинга курьеров — Задание 2;
3) Rate limiter для сервиса — Задание 3 (используеются resilience4j);
4) Алгоритм распределения заказов по курьерам — Задание 4 (используется jsprit).

## Запуск сервиса (порт 8080)

### Используя docker-compose 
* git clone https://github.com/ivshapovalov/yandex-lavka.git
* cd yandex-lavka
* docker-compose up 

### Используя docker (имеется отдельная база данных Postgres)

* git clone https://github.com/ivshapovalov/yandex-lavka.git
* cd yandex-lavka
* заполнить в Dockerfile переменные подключения к базе
* docker build -t yandex-lavka .
* docker run -p 8080:8080 yandex-lavka

### В папке  [здесь](https://github.com/ivshapovalov/yandex-lavka/tree/main/docs/postman/) варианты запросов, которые можно загрузить как коллекции в Postman