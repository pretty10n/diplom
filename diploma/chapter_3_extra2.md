## 3.1 Обоснование инструментальных средств разработки системы

Maven выбран как стандарт сборки Java-проектов в корпоративной среде. Файл pom.xml фиксирует версии spring-boot-starter-parent 3.3.5, зависимости spring-boot-starter-web, data-jpa, validation, flyway, postgresql, poi-ooxml, springdoc. Плагин spring-boot-maven-plugin формирует исполняемый jar. Wrapper не обязателен при установленном Maven 3.9+.

Vite выбран вместо Create React App ввиду более быстрой сборки и нативной поддержки TypeScript. Конфигурация vite.config.ts задаёт proxy: { '/api': 'http://localhost:8080' }, что позволяет frontend обращаться к backend без настройки CORS в dev.

Flyway-миграции версионированы префиксом V и двойным подчёркиванием. Скрипт V1 создаёт шесть таблиц и начальные справочники разделов; V2 выполняет UPDATE active=false для return_waste_f4/f5; V3 создаёт reference_supplier и reference_material с CHECK на формат ИНН, частичными уникальными индексами и индексами для полнотекстового поиска по подстроке. Откат миграций в MVP не автоматизирован — допустимо для учебного проекта.

Выбор BigDecimal вместо double на сервере обоснован требованием точности денежных расчётов. Double мог бы давать артефакты 0.30000000004 при суммировании, недопустимые в отчётности ГОЗ.

## 3.2 Описание работы программы

Полный перечень REST endpoint документов (DocumentController): POST /api/v1/documents; GET /api/v1/documents/{id}; PATCH /api/v1/documents/{id}/common; POST /api/v1/documents/{id}/entries; PATCH /api/v1/documents/{id}/entries/{entryId}; DELETE /api/v1/documents/{id}/entries/{entryId}; GET /api/v1/documents/{id}/entries с query sectionKey, derivedFormNo, status; GET /api/v1/documents/{id}/totals; GET /api/v1/dictionaries; POST /api/v1/documents/{id}/export; GET /api/v1/files/{fileId}. Контур справочников (ReferenceDataController): GET /reference-data/suppliers/search и /materials/search; GET /reference-data/suppliers и /materials; POST, PATCH, DELETE для каждой сущности по id.

Структура JSON fields строки: col2 (наименование), col3–col10 (числовые и текстовые графы по форме), col13_1, col13_2 (код и расшифровка условия закупки), col14 (дополнительный показатель), col15 (ИНН поставщика). Структура computed: col11, col12 как числа с двумя знаками после запятой.

Алгоритм exportFilledTemplate: 1) открыть InputStream шаблона из classpath; 2) для каждого листа формы найти таблицу данных; 3) записать common block в фиксированные ячейки; 4) для каждой entry с matching section записать row в следующую свободную строку; 5) записать totals в итоговые ячейки; 6) вернуть ByteArrayOutputStream.

Состояния UI шага 2: режим «добавление» (пустая форма), режим «редактирование» (заполнение из selectedEntry), режим «дублирование» (копия fields без id), режим «управление справочниками» (модальное окно ReferenceDataManager поверх шага 2). Переключение сбрасывает dirty-флаг только после успешного ответа API. Компонент AutocompleteInput реализует debounce 250 мс, закрытие списка при клике вне поля и атрибуты доступности role=combobox, aria-expanded, aria-controls.

Логирование: стандартный Spring Boot logging уровня INFO для запросов; ошибки валидации не логируются как ERROR, чтобы не засорять журнал ожидаемыми пользовательскими ошибками.

## 3.3 Системные требования

Клиентские ОС: Windows 10/11, macOS 13+, Linux с графическим браузером. Мобильные браузеры не поддерживаются официально — таблица строк требует ширины desktop.

Серверные ОС: Linux предпочтителен для production; Windows Server допустим для пилотного внедрения. Требуется установленный JRE/JDK 21 для запуска jar.

Безопасность MVP: отсутствует аутентификация — документ доступен любому, кто знает UUID (security through obscurity). Для production обязательны: OAuth2/OIDC, проверка прав на documentId, HTTPS, маскирование ИНН в логах.

Масштабирование: вертикальное (увеличение RAM/CPU) достаточно для сотен документов; горизонтальное требует вынесения ExportFileStore в S3-совместимое хранилище и sticky sessions или stateless export.

Мониторинг: рекомендуется Spring Boot Actuator с endpoints health, metrics; алерт при ошибках 5xx > 1% запросов.
