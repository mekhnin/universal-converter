![build](https://github.com/mekhnin/universal-converter/actions/workflows/.github/workflows/maven.yml/badge.svg)

# Universal Converter
Универсальный HTTP-сервис конвертации единиц измерения.

## API
Сервис предоставляет один метод `POST /convert` с JSON в теле запроса: 
```json
{
 "from": "<выражение в исходных единицах>",
 "to": "<выражение в единицах, которые необходимо получить>"
}
```

Варианты ответа:
- Код `400 Bad Request`, если в выражениях используются неизвестные единицы измерения.
- Код `404 Not Found`, если невозможно осуществить преобразование.
- Код `200 OK`, в теле ответа коэффициент преобразования.

Пример тела запроса:
```json
{
 "from": "м / с",
 "to":  "км / час"
}
```
Пример тела ответа:
```text
3.6
```

## Сборка и запуск
- Компиляция кода и его исполнение c использованием Java 11.
- Сборка сервиса при помощи Apache Maven командой `mvn package`.
- Запуск сервиса осуществляется командой `java -jar universal-converter-1.0.0.jar /path/to/file.csv`,
где `/path/to/file.csv` – путь до файла с правилами конвертации.
- Сервис принимает HTTP-запросы на порту 80.

