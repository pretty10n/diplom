# API-контракты (MVP)

## Базовые принципы
- Формат обмена: JSON
- Версия API: `/api/v1`
- Идентификаторы: UUID
- Даты: ISO 8601
- Scope MVP: только формы 4, 5, 6.

## Сущности
- `Document` — документ с общими данными и строками
- `Entry` — универсальная строка ввода
- `Totals` — расчетные итоги по формам

## 1) Создать документ
`POST /api/v1/documents`

Request:
```json
{
  "common": {
    "truName": "Наименование",
    "truCode": "Шифр",
    "stage": "Этап 1",
    "reportYear": 2026,
    "planYear": 2027
  }
}
```

Response:
```json
{
  "id": "5d73f711-d12d-4871-a96d-5f53ddd568e5",
  "status": "draft",
  "createdAt": "2026-04-16T10:00:00Z"
}
```

## 2) Получить документ
`GET /api/v1/documents/{documentId}`

Response:
```json
{
  "id": "5d73f711-d12d-4871-a96d-5f53ddd568e5",
  "status": "draft",
  "common": {
    "truName": "Наименование",
    "truCode": "Шифр",
    "stage": "Этап 1",
    "reportYear": 2026,
    "planYear": 2027
  },
  "entriesCount": 12
}
```

## 3) Обновить общие данные
`PATCH /api/v1/documents/{documentId}/common`

Request:
```json
{
  "truName": "Новое наименование",
  "truCode": "Новый шифр",
  "stage": "Этап 2",
  "reportYear": 2026,
  "planYear": 2028
}
```

Response: `200 OK`

## 4) Добавить строку
`POST /api/v1/documents/{documentId}/entries`

### Входная модель Entry
Ключевой принцип: поле формы не передается, форма определяется сервером по `sectionKey`.

Request:
```json
{
  "sectionKey": "raw_materials",
  "fields": {
    "col2": "Наименование позиции",
    "col3": "текст",
    "col4": "текст",
    "col6": 10,
    "col7": 2.5,
    "col8": 2.8,
    "col9": 100,
    "col10": 110,
    "col13_1": "Документ №1",
    "col13_2": "LIST_VALUE",
    "col14": "ООО Поставщик",
    "col15": "7701234567"
  }
}
```

Response:
```json
{
  "entryId": "8dc76d8f-c213-48b6-a3b8-49789c2a2137",
  "formNo": 4,
  "sectionNo": 1,
  "rowNo": "1.1",
  "computed": {
    "col11": 250,
    "col12": 308
  }
}
```

## 5) Обновить строку
`PATCH /api/v1/documents/{documentId}/entries/{entryId}`

Request:
```json
{
  "sectionKey": "raw_materials",
  "fields": {
    "col2": "Обновленное наименование",
    "col7": 3,
    "col9": 120
  }
}
```

Response:
```json
{
  "entryId": "8dc76d8f-c213-48b6-a3b8-49789c2a2137",
  "rowNo": "1.1",
  "computed": {
    "col11": 360,
    "col12": 308
  }
}
```

## 6) Удалить строку
`DELETE /api/v1/documents/{documentId}/entries/{entryId}`

Response:
```json
{
  "deleted": true,
  "renumbered": true
}
```

## 7) Получить список строк
`GET /api/v1/documents/{documentId}/entries?sectionKey=&derivedFormNo=&status=`

Response:
```json
{
  "items": [
    {
      "entryId": "8dc76d8f-c213-48b6-a3b8-49789c2a2137",
      "sectionKey": "raw_materials",
      "formNo": 4,
      "sectionNo": 1,
      "rowNo": "1.1",
      "fields": {
        "col2": "Наименование позиции"
      },
      "computed": {
        "col11": 250,
        "col12": 308
      },
      "validationStatus": "valid"
    }
  ],
  "total": 1
}
```

## 8) Получить итоги по формам 4/5/6
`GET /api/v1/documents/{documentId}/totals`

Response:
```json
{
  "form4": {
    "section1Total": { "col11": 1000, "col12": 1200 },
    "section2Total": { "col11": 500, "col12": 600 },
    "section3Total": { "col11": 100, "col12": 120 },
    "section1And2Total": { "col11": 1500, "col12": 1800 }
  },
  "form5": {
    "section1Total": { "col11": 700, "col12": 800 },
    "section2Total": { "col11": 70, "col12": 80 }
  },
  "form6": {
    "total": { "col11": 300, "col12": 330 }
  }
}
```

## 9) Экспорт в XLSX
`POST /api/v1/documents/{documentId}/export`

Request:
```json
{
  "format": "xlsx",
  "mode": "fill_template",
  "templateFileId": "b1f23f4f-6f0d-4f9b-9d1e-7b4ed2d9a8b2",
  "fileName": "report_2026_filled.xlsx"
}
```

Response:
```json
{
  "downloadUrl": "/api/v1/files/8fefc4ea-3acf-4fcb-a79f-8eb3ff5fcbf9",
  "expiresAt": "2026-04-16T12:00:00Z",
  "exportMeta": {
    "templatePreserved": true,
    "updatedSheets": ["Форма 4", "Форма 5", "Форма 6"]
  }
}
```

Требования к экспорту:
- Экспорт работает только в режиме заполнения предоставленного шаблона (`mode=fill_template`).
- Макет Excel должен сохраняться полностью: стили, объединения, размеры строк/столбцов, формулы, печатные настройки, скрытые листы, именованные диапазоны.
- Backend изменяет только заранее определенные целевые ячейки.
- Если шаблон не соответствует ожидаемой структуре, API возвращает `VALIDATION_ERROR` с описанием проблемных листов/ячеек.

## 10) Справочники для выпадающих списков
`GET /api/v1/dictionaries`

Response:
```json
{
  "sectionKeys": [
    { "key": "raw_materials", "label": "Сырье и основные материалы", "formNo": 4, "sectionNo": 1 },
    { "key": "aux_materials", "label": "Вспомогательные материалы", "formNo": 4, "sectionNo": 2 },
    { "key": "return_waste_f4", "label": "Возвратные отходы (вычитаются) [Форма 4]", "formNo": 4, "sectionNo": 3 },
    { "key": "purchased_semi", "label": "Покупные полуфабрикаты", "formNo": 5, "sectionNo": 1 },
    { "key": "return_waste_f5", "label": "Возвратные отходы (вычитаются) [Форма 5]", "formNo": 5, "sectionNo": 2 },
    { "key": "components", "label": "Приобретение комплектующих изделий", "formNo": 6, "sectionNo": 1 }
  ],
  "col13_2Values": [],
  "col5_2Values": []
}
```

## Ошибки API
Формат:
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Поле col3 должно быть числом",
    "details": [
      { "field": "fields.col3", "reason": "not_a_number" }
    ]
  }
}
```

Коды:
- `VALIDATION_ERROR` — ошибка валидации
- `NOT_FOUND` — ресурс не найден
- `CONFLICT` — конфликт состояния
- `INTERNAL_ERROR` — внутренняя ошибка

## Правила расчета на backend
- Форма определяется по `sectionKey`.
- Нумерация назначается сервером.
- Формулы колонок 11/12 считает сервер.
- Итоги считает сервер.
