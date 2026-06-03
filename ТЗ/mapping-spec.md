# Mapping Spec (формы 4, 5, 6)

## Статус и источник
- Источник: `Приложение_№_5_к_Приказа_от_16.12.2022_№_995.22-1673362953 (1).xlsx`.
- Используемые листы:
  - Ф.4 -> `xl/worksheets/sheet6.xml`
  - Ф.5 -> `xl/worksheets/sheet8.xml`
  - Ф.6 -> `xl/worksheets/sheet10.xml`

## Единая каноническая модель (для backend)

### packageHeader
- `productNameAndCode`: общий идентификатор объекта расчета (наименование/шифр).
- `okpOrOkpd2`: код ОКП/ОКПД2.
- `ekps`: код ЕКПС (опционально).
- `fnn`: ФНН (опционально).
- `unit`: единица измерения.
- `stage`: этап (если применимо для контракта/работы).
- `reportYear`: отчетный год.
- `planYear`: планируемый год.



## Маппинг заголовков (header block)

### Для форм 4/5/6
- Общий блок расположен в верхней части листа:
  - Наименование/шифр: подпись `(наименование, шифр товара, работы, услуги)` в диапазонах:
    - Ф.4: `CF6:FD6`
    - Ф.5: `CG6:FD6`
    - Ф.6: `CG6:FD6`
  - Коды и единица измерения в заголовке таблицы:
    - Ф.4: `AF9` (ОКП/ОКПД2), `AL9` (ЕКПС), `AR9` (ФНН), `AX9` (ед. изм.)
    - Ф.5: `AF9`, `AL9`, `AR9`, `AX9`
    - Ф.6: `AJ9`, `AP9`, `AV9`, `BB9`


## Маппинг табличных блоков источников (4/5/6)

### Форма 4 (сырье, материалы, вспомогательные материалы)
- Ключевые колонки:
  - `itemName`: `G` (строки данных, начиная с 15)
  - `normPlan`: `BG`
  - `normFact`: `BO`
  - `costPlan`: `CM`
  - `costFact`: `CU`
  - `supplierJustificationDoc`: `DC`
  - `supplierPricingMethod`: `DV`
  - `supplierName`: `EF`
  - `supplierInn`: `EO`
  - `forecastNorm`: `FJ`
  - `forecastCost`: `GB`
  - `forecastJustificationDoc`: `GK`
  - `forecastPricingMethod`: `HC`
  - `forecastSupplierName`: `HM`
  - `forecastSupplierInn`: `HV`

### Форма 5 (покупные полуфабрикаты)
- Ключевые колонки:
  - `itemName`: `G`
  - `normPlan`: `BG`
  - `normFact`: `BO`
  - `costPlan`: `CM`
  - `costFact`: `CU`
  - `supplierJustificationDoc`: `DC`
  - `supplierPricingMethod`: `DT`
  - `supplierName`: `EE`
  - `supplierInn`: `EQ`
  - `forecastNorm`: `FK`
  - `forecastCost`: `GC`
  - `forecastJustificationDoc`: `GL`
  - `forecastPricingMethod`: `HB`
  - `forecastSupplierName`: `HM`
  - `forecastSupplierInn`: `HY`

### Форма 6 (покупные комплектующие изделия)
- Ключевые колонки:
  - `itemName`: `G`
  - `normPlan`: `BK`
  - `normFact`: `BS`
  - `costPlan`: `CQ`
  - `costFact`: `CY`
  - `supplierJustificationDoc`: `DG`
  - `supplierPricingMethod`: `DU`
  - `supplierName`: `EF`
  - `supplierInn`: `ER`
  - `forecastNorm`: `FL`
  - `forecastCost`: `GD`
  - `forecastJustificationDoc`: `GM`
  - `forecastPricingMethod`: `HB`
  - `forecastSupplierName`: `HM`
  - `forecastSupplierInn`: `HY`


### Базовые правила преобразования
- `0100 Материальные затраты - всего` = сумма релевантных затрат из Ф.4 + Ф.5 + Ф.6:
  - Ф.4: разделы сырья/вспомогательных материалов (строки под `G15..G29`, числовые колонки `CM/CU/GB` по режиму расчета).
  - Ф.5: разделы полуфабрикатов (`G15..G24`, колонки `CM/CU/GC`).
  - Ф.6: раздел комплектующих (`G14..G18`, колонки `CQ/CY/GD`).
- В MVP исключены формы `1`, `2`, `7`, `20`; маппинг и расчеты выполняются только для форм `4`, `5`, `6`.

### Режим выбора значения
- `previewMode = PLAN`: брать плановые колонки (`CM`, `CQ`, `AN` и аналоги).
- `previewMode = FACT`: брать фактические колонки (`CU`, `CY`, `AZ` и аналоги).
- `previewMode = FORECAST`: брать прогнозные/планируемые колонки (`GB`, `GC`, `GD`, `EO` и аналоги).


## Правила добавления строк
- Для форм 4/5/6 строка создается в первой пустой строке после последней заполненной в основном блоке.
- `rowNo` пересчитывается последовательно (1, 2, 3, ...).
- Итоговые строки `ИТОГО` не являются пользовательскими и вычисляются системой.

## Edge cases и решения
- Отсутствуют строки в источнике:
  - значение в цели = `0`;
  - `traceSource` содержит запись с `rule = NO_SOURCE_ROWS`.
- Пустые/нулевые plan/fact:
  - пустое -> трактуется как `0`;
  - null-safe суммирование.
- Различия в названиях листов (`д` и без `д`):
  - сопоставление по префиксу `Ф.<номер>` и fallback по `sheetId`.

## Ограничения текущей версии
- Поля ручной корректировки после автозаполнения не зафиксированы (open question TZ-001).
