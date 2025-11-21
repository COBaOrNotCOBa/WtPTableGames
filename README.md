# WtP TableGames

Экспериментальный проект с Телеграм-ботом про настольные игры. Здесь я больше пробовал подходы, структуру и работу с Telegram Bot API, чем доводил продукт до готового состояния.

## Что делает проект

- Отправляет запросы к Telegram Bot API через HTTP
- Обменивается данными в формате JSON
- Реализует базовую логику бота на Kotlin в виде консольного JVM-приложения (`main` в `MainKt`)

(Конкретные команды и сценарии можно описать отдельно, если захочется подробнее.)

## Технологический стек

- **Язык:** Kotlin (JVM) `1.8.21`
- **JVM:** JDK 18 (`jvmToolchain(18)`)
- **Сборка:** Gradle Kotlin DSL
- **Библиотеки:**
  - `kotlinx-serialization-json` — работа с JSON
  - `okhttp` — HTTP-клиент для запросов к Telegram Bot API
  - `kotlin("test")` — для тестов
- **Плагины Gradle:**
  - `application` — запуск как обычного JVM-приложения
  - `com.github.johnrengelman.shadow` — сборка fat-jar (если настроен таск `shadowJar`)

## Требования

- Установленный **JDK 18**
- Доступ в Интернет (для работы с Telegram)
- **Gradle Wrapper** в проекте  
  (если вдруг его нет, можно добавить через команду `./gradlew wrapper --gradle-version latest`)

## Как запустить

### Клонирование репозитория

    git clone https://github.com/COBaOrNotCOBa/WtPTableGames.git
    cd WtPTableGames

### Запуск через Gradle Wrapper

Linux / macOS:

    ./gradlew run

Windows:

    gradlew.bat run

Приложение запустится с точкой входа `MainKt`, указанной в блоке `application` Gradle-скрипта.

### (Опционально) Сборка fat-jar

Если настроен таск `shadowJar`, можно собрать единый jar.

Linux / macOS:

    ./gradlew shadowJar

Windows:

    gradlew.bat shadowJar

Готовый jar-файл будет лежать в каталоге:

    build/libs/

## Статус проекта

Незавершённый эксперимент с Телеграм-ботом и настольными играми. Репозиторий больше про «песочницу» и пробу стека, чем про готовое приложение.
