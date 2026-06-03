# TZ-003 CRUD API для форм 4/5/6

## Status
COMPLETED

## Goal
Реализовать API v1 для управления документом, общими данными и универсальными строками форм 4/5/6 согласно `API_SPEC.md`.

## Business context
Пользователь должен вносить данные в интерфейсе и сохранять их в системе для дальнейших расчетов и экспорта.

## System context
REST API backend, потребляется React-приложением.

## Functional requirements
- Реализовать endpoint'ы:
  - `POST /api/v1/documents` — создание документа;
  - `GET /api/v1/documents/{documentId}` — получение документа;
  - `PATCH /api/v1/documents/{documentId}/common` — обновление общих данных;
  - `POST /api/v1/documents/{documentId}/entries` — добавление универсальной строки;
  - `PATCH /api/v1/documents/{documentId}/entries/{entryId}` — обновление строки;
  - `DELETE /api/v1/documents/{documentId}/entries/{entryId}` — удаление с перенумерацией;
  - `GET /api/v1/documents/{documentId}/entries` — список строк с фильтрами;
  - `GET /api/v1/documents/{documentId}/totals` — получение итогов по формам 4/5/6;
  - `GET /api/v1/dictionaries` — справочники для UI.
- Поле формы не принимается во входе строки; форма и раздел выводятся сервером по `sectionKey`.
- Нумерация строк назначается только сервером и возвращается как `rowNo`.
- Расчетные поля возвращаются в блоке `computed` и не принимаются как пользовательский ввод.
- Реализовать фильтры списка строк: `sectionKey`, `derivedFormNo`, `status`.

## Non-functional requirements
- API документировано через OpenAPI.
- Возврат понятных ошибок валидации.
- Формат обмена JSON, идентификаторы UUID, даты ISO 8601.
- Единый формат ошибок:
  - `VALIDATION_ERROR`,
  - `NOT_FOUND`,
  - `CONFLICT`,
  - `INTERNAL_ERROR`.

## Acceptance criteria
- Все endpoint'ы из списка реализованы и проходят контрактные проверки.
- UI может создать документ, редактировать общий блок и выполнять полный CRUD по универсальным строкам без потери данных для форм 4/5/6.
- При удалении строки API возвращает `renumbered=true` и список строк отражает новую нумерацию.

## Edge cases
- Частичное заполнение строки.
- Обновление несуществующего row-id.
- Передан неизвестный `sectionKey`.
- Для числовых полей передано нечисловое значение.

## Dependencies and risks
- Зависит от TZ-002.

## Open questions
- Нужен ли soft-delete строк или достаточно физического удаления?
