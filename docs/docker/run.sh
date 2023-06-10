#!/bin/bash
docker-compose down --volumes
docker rm -f yandex-lavka-db
docker rm -f yandex-lavka-app
docker-compose  up --build --remove-orphans




