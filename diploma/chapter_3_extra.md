## 3.1 Обоснование инструментальных средств разработки системы

Java 21 выбрана как LTS-версия с поддержкой современных языковых конструкций (records, pattern matching), используемых в DTO API. Spring Boot 3.3 работает на базе Jakarta EE 10, что обеспечивает совместимость с долгосрочной поддержкой корпоративного стека. В отличие от микрофреймворков (Spark, Javalin), Spring предоставляет готовую интеграцию JPA, validation и exception handling, сократив время разработки MVP до девяти завершённых ТЗ.

React выбран вместо классического JSF/Thymeleaf, поскольку мастер ввода требует динамического изменения набора полей без перезагрузки страницы. Альтернатива Vue.js была допустима, однако TypeScript-экосистема React в сочетании с Vite обеспечила более быструю настройку прокси и HMR при разработке TZ-007 и TZ-008.

PostgreSQL предпочтён MySQL/MariaDB из-за выразительности JSONB и строгой проверки ограничений. MS SQL Server не рассматривался ввиду лицензионных ограничений учебного стенда. SQLite отклонён как несоответствующий многопользовательской эксплуатации.

Apache POI выбран вместо JExcelAPI и docx4j (последний ориентирован на Word). Библиотека активно поддерживается, совместима с Office Open XML и позволяет открывать поток из classpath без промежуточного копирования на диск. Недостаток POI — повышенное потребление памяти при больших шаблонах; для шаблона форм 4/5/6 размером в пределах нескольких мегабайт это не критично.

Инструменты разработки: IntelliJ IDEA / VS Code, Git, Maven Wrapper, npm. Документирование API — springdoc-openapi-starter-webmvc-ui. Тесты запускаются через mvn test в CI-пайплайне.

## 3.2 Описание работы программы

Модуль core-form-service стартует из класса jd.ru.App. При запуске Spring Boot поднимает embedded Tomcat на порту 8080 (application.yml), применяет Flyway-миграции и публикует Swagger UI. DocumentController регистрирует endpoint'ы работы с документами; ReferenceDataController — десять endpoint'ов справочников по пути /api/v1/reference-data (поиск, список, создание, обновление, удаление для suppliers и materials). Каждый метод делегирует вызов DocumentApiService или ReferenceDataService, оборачивая ответы в ResponseEntity с корректными HTTP-кодами.

Создание документа (POST /documents) генерирует UUID, сохраняет статус draft, создаёт связанную запись common info. Поле truCode в MVP может быть пустой строкой при объединённом вводе наименования и шифра в UI — данное упрощение задокументировано в API_SPEC. Обновление common (PATCH) валидирует годы и обязательные строковые поля через Jakarta Validation.

Операция добавления строки (POST /entries) выполняет цепочку: resolve sectionKey → reject excluded → validate decimals → validate INN → compute col11/col12 → assign rowNo → persist → syncFromEntryFields → return UpsertEntryResponse с validationStatus. Обновление (PATCH) запрещает изменение sectionKey, чтобы не нарушить историю нумерации и привязку к форме; после сохранения также вызывается syncFromEntryFields.

Метод exportDocument проверяет format=xlsx и mode=fill_template, загружает все строки документа без фильтра, передаёт в XlsxTemplateExportService. Сервис экспорта определяет листы по синонимам названий («Форма 4», «Ф.4» и т.д.), заполняет ячейки, возвращает byte[]. ExportFileStore сохраняет файл под fileId с TTL для последующей загрузки.

Frontend App.tsx управляет состоянием currentStep (1..3), documentId, entries[], totals, previewReadyForExport, referenceDataOpen. API-клиент client.ts реализует функции createDocument, updateCommon, listEntries, upsertEntry, deleteEntry, getTotals, exportDocument, downloadFile, а также searchSuppliers, searchMaterials, listSuppliers, listMaterials и CRUD справочников. Типы в types/api.ts синхронизированы с OpenAPI-спецификацией.

Особенности шага 2: динамическая форма отображает разный набор подсказок для форм 4/5 и 6; col11/col12 подсвечиваются как вычисляемые; поля col2, col14 и col15 используют AutocompleteInput с поддержкой клавиш ArrowUp/ArrowDown, Enter и Escape; при дублировании строки создаётся копия fields без rowNo. Особенности шага 3: параллельный fetch entries и totals через Promise.all; список invalid-строк с кнопкой «Исправить» переводит на шаг 2 с выделением entryId.

## 3.3 Системные требования

Аппаратная конфигурация стенда разработки (фактическая): ПК с 16 ГБ RAM, 8 логических ядер CPU, SSD 512 ГБ; ОС Windows 11. Backend и PostgreSQL запускались локально; frontend — через Vite dev-server на порту 5173. Данная конфигурация избыточна для MVP, но обеспечивает комфортную разработку с параллельным запуском IDE, браузера и СУБД.

Сетевая топология учебного стенда: localhost-only. В промышленной топологии рекомендуется reverse proxy (nginx) с TLS-терминацией, отдельная подсеть для PostgreSQL, ограничение Swagger UI административной сетью.

Программные зависимости зафиксированы в pom.xml и package.json. Backend требует Java 21 (toolchains.xml). Frontend — Node 20 LTS. База данных — PostgreSQL 15 с расширением uuid-ossp при необходимости генерации UUID на стороне БД (в MVP UUID генерирует приложение).

Эксплуатационные процедуры: резервное копирование таблиц document* и dictionary*; мониторинг дискового пространства ExportFileStore; ротация логов Spring Boot. Процедуры выходят за рамки MVP, но включены в рекомендации для внедрения на предприятии ОПК.

Качество и приёмка: интеграционные сценарии TZ-009 подтверждают сквозной путь BP-01. Unit- и web-тесты backend покрывают контроллер и структуру экспорта. Frontend на этапе MVP проверялся вручную по чек-листу UI_SPEC.
