# Прототип АИС учёта региональных субсидированных авиаперевозок (REST API)

## Стек технологий:
- Java 22
- Spring Boot 4.1.0
- PostgreSQL 18.3
- Swagger (Open API) 3.0.2
- JUnit 6.0.3

## Перед запуском:
- Запустить Docker контейнер с PostgreSQL базой данных. Для этого в терминале или в
Docker Desktop необходимо ввести команду 
`docker run --name subsides -p 5432:5432 -e POSTGRES_USER=YOUR_USERNAME
-e POSTGRES_PASSWORD=YOUR_PASSWORD -d postgres`
- Настроить переменные окружение. Для этого необходимо создать файл `.env` и заполнить его
в соответствии с шаблоном `.env.example`, после чего указать файл `.env` в конфигурации
запуска

## Swagger (OpenAPI)
Смотреть спецификацию API: `http://localhost:8080/swagger-ui/index.html#/`