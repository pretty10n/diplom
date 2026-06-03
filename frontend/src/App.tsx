import { useEffect, useMemo, useState } from "react";
import {
  addEntry,
  createDocument,
  deleteEntry,
  exportDocument,
  getDocument,
  getDictionaries,
  getTotals,
  listEntries,
  updateCommon,
  updateEntry
} from "./api/client";
import type {
  CommonInfoApiPayload,
  CommonInfoForm,
  DictionariesResponse,
  EntryFields,
  EntryItem,
  StoredDocumentCommonInfo,
  TotalsResponse
} from "./types/api";

const emptyCommon: CommonInfoForm = {
  truNameAndCode: "",
  stage: "",
  reportYear: "",
  planYear: ""
};

const editableColumns = ["col2", "col3", "col4", "col5", "col6", "col7", "col8", "col9", "col10", "col13_1", "col13_2", "col14", "col15"];
const numericColumns = new Set(["col7", "col8", "col9", "col10"]);
const twoDecimalColumns = new Set(["col9", "col10"]);
const excludedSectionKeys = new Set(["return_waste_f4", "return_waste_f5"]);
const placeholdersForDash = new Set(["col3", "col4"]);
const col13_2Options = [
  "метод анализа рыночных индикаторов",
  "метод сравнимой цены",
  "затратный метод",
  "метод индексации базовой цены",
  "метод индексации по статьям базовых затрат"
];
const columnLabels: Record<string, string> = {
  col2: " наименование",
  col3: " Код ОКП/ОКПД2",
  col4: " Код ЕКПС (при наличии)",
  col5: "ФНН (при наличии)",
  col6: " Единица измерения",
  col7: "расход на единицу продукции - план",
  col8: "расход на единицу продукции - факт",
  col9: "цена за единицу измерения (руб.) - план",
  col10: "цена за единицу измерения (руб.) - факт",
  col11: "затраты (руб.) - план",
  col12: "затраты (руб.) - факт",
  col13_1: "первичные документы (номер и дата договора,  протокола, счета, иное)",
  col13_2: "метод определения цены",
  col14: "наименование организации-поставщика",
  col15: "ИНН организации-поставщика"
};

function App() {
  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [documentId, setDocumentId] = useState("");
  const [common, setCommon] = useState<CommonInfoForm>(emptyCommon);
  const [dictionaries, setDictionaries] = useState<DictionariesResponse | null>(null);
  const [entries, setEntries] = useState<EntryItem[]>([]);
  const [filters, setFilters] = useState({ sectionKey: "", derivedFormNo: "", status: "" });
  const [sectionKey, setSectionKey] = useState("");
  const [editingId, setEditingId] = useState<string | null>(null);
  const [entryFields, setEntryFields] = useState<EntryFields>({});
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [dirty, setDirty] = useState(false);
  const [loadDocumentId, setLoadDocumentId] = useState("");
  const [totals, setTotals] = useState<TotalsResponse | null>(null);
  const [exporting, setExporting] = useState(false);
  const [previewReadyForExport, setPreviewReadyForExport] = useState(false);

  const visibleSectionKeys = useMemo(
    () => dictionaries?.sectionKeys.filter((s) => !excludedSectionKeys.has(s.key)) ?? [],
    [dictionaries]
  );

  const selectedSection = useMemo(
    () => visibleSectionKeys.find((x) => x.key === sectionKey) ?? null,
    [visibleSectionKeys, sectionKey]
  );

  const entryCounts = useMemo(() => {
    const countByKey = (key: string) => entries.filter((entry) => entry.sectionKey === key).length;
    return {
      form4: {
        section1: countByKey("raw_materials"),
        section2: countByKey("aux_materials")
      },
      form5: { section1: countByKey("purchased_semi") },
      form6: { total: countByKey("components") }
    };
  }, [entries]);

  useEffect(() => {
    const onBeforeUnload = (event: BeforeUnloadEvent) => {
      if (!dirty) return;
      event.preventDefault();
      event.returnValue = "";
    };
    window.addEventListener("beforeunload", onBeforeUnload);
    return () => window.removeEventListener("beforeunload", onBeforeUnload);
  }, [dirty]);

  useEffect(() => {
    if (step !== 2 || !documentId) return;
    void loadDictionaries();
    void loadEntries();
  }, [step, documentId]);

  useEffect(() => {
    if (step === 2 && documentId) {
      void loadEntries();
    }
  }, [filters]);

  useEffect(() => {
    if (step === 3 && documentId) {
      void loadPreviewData();
    }
  }, [step, documentId]);

  async function loadDocumentById() {
    if (!loadDocumentId.trim()) {
      setError("Введите id документа.");
      return;
    }
    setLoading(true);
    setError("");
    try {
      const doc = await getDocument(loadDocumentId.trim());
      setDocumentId(doc.id);
      setCommon(commonFormFromApi(doc.common));
      setMessage("Документ загружен.");
      setDirty(false);
    } catch (e) {
      setError(`Ошибка загрузки документа: ${toMessage(e)}`);
    } finally {
      setLoading(false);
    }
  }

  async function saveCommon(goNext: boolean) {
    setError("");
    setMessage("");
    if (!isCommonValid(common)) {
      setError("Заполните обязательные поля и проверьте год.");
      return;
    }
    setLoading(true);
    try {
      if (!documentId) {
        const created = await createDocument(normalizeCommon(common));
        setDocumentId(created.id);
      } else {
        await updateCommon(documentId, normalizeCommon(common));
      }
      setPreviewReadyForExport(false);
      setDirty(false);
      setMessage("Черновик общих данных сохранен.");
      if (goNext) setStep(2);
    } catch (e) {
      setError(toMessage(e));
    } finally {
      setLoading(false);
    }
  }

  async function loadDictionaries() {
    try {
      const dicts = await getDictionaries();
      setDictionaries(dicts);
    } catch (e) {
      setError(`Ошибка загрузки справочников: ${toMessage(e)}`);
    }
  }

  async function loadEntries() {
    if (!documentId) return;
    setLoading(true);
    setError("");
    try {
      const response = await listEntries(documentId, filters);
      setEntries(response.items);
    } catch (e) {
      setError(`Ошибка загрузки строк: ${toMessage(e)}`);
    } finally {
      setLoading(false);
    }
  }

  async function loadPreviewData() {
    if (!documentId) return;
    setLoading(true);
    setError("");
    try {
      const [entriesResponse, totalsResponse] = await Promise.all([
        listEntries(documentId, {}),
        getTotals(documentId)
      ]);
      setEntries(entriesResponse.items);
      setTotals(totalsResponse);
      setPreviewReadyForExport(true);
    } catch (e) {
      setError(`Ошибка загрузки предпросмотра: ${toMessage(e)}`);
      setPreviewReadyForExport(false);
    } finally {
      setLoading(false);
    }
  }

  async function submitEntry() {
    if (!documentId) {
      setError("Сначала сохраните шаг 1.");
      return;
    }
    if (!selectedSection) {
      setError("Выберите раздел.");
      return;
    }
    if (!entryFields.col2) {
      setError("Поле col2 обязательно.");
      return;
    }
    if (!isValidSupplierInn(entryFields.col15)) {
      setError("ИНН организации-поставщика: допустимы только 10 или 12 цифр (или оставьте поле пустым).");
      return;
    }

    const payload = {
      sectionKey,
      fields: normalizeFields(entryFields)
    };

    setLoading(true);
    setError("");
    try {
      if (editingId) {
        await updateEntry(documentId, editingId, payload);
        setMessage("Строка обновлена.");
      } else {
        await addEntry(documentId, payload);
        setMessage("Строка добавлена.");
      }
      setEntryFields({});
      setEditingId(null);
      setSectionKey("");
      await loadEntries();
      setPreviewReadyForExport(false);
      setDirty(false);
    } catch (e) {
      setError(`Ошибка сохранения строки: ${toMessage(e)}`);
    } finally {
      setLoading(false);
    }
  }

  async function removeEntry(entryId: string) {
    if (!documentId) return;
    if (!window.confirm("Удалить строку?")) return;
    setLoading(true);
    try {
      await deleteEntry(documentId, entryId);
      setMessage("Строка удалена.");
      await loadEntries();
      setPreviewReadyForExport(false);
    } catch (e) {
      setError(`Ошибка удаления: ${toMessage(e)}`);
    } finally {
      setLoading(false);
    }
  }

  async function handleExport() {
    if (!documentId) {
      setError("Нет документа для экспорта.");
      return;
    }
    if (!previewReadyForExport) {
      setError("Обновите агрегаты перед экспортом, чтобы выгрузка соответствовала последнему preview.");
      return;
    }
    setExporting(true);
    setError("");
    try {
      const blob = await exportDocument(documentId, `document-${documentId}.xlsx`);
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `document-${documentId}.xlsx`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
      setMessage("Excel сформирован.");
    } catch (e) {
      setError(`Ошибка экспорта: ${toMessage(e)}`);
    } finally {
      setExporting(false);
    }
  }

  function onEdit(item: EntryItem) {
    setEditingId(item.entryId);
    setSectionKey(item.sectionKey);
    setEntryFields(item.fields ?? {});
  }

  function onDuplicate(item: EntryItem) {
    setEditingId(null);
    setSectionKey(item.sectionKey);
    setEntryFields(item.fields ?? {});
  }

  function jumpToEntry(item: EntryItem) {
    setStep(2);
    setEditingId(item.entryId);
    setSectionKey(item.sectionKey);
    setEntryFields(item.fields ?? {});
  }

  const validationErrors = entries.filter((item) => item.validationStatus === "invalid");
  const col11Preview = getComputedPreview(entryFields.col7, entryFields.col9);
  const col12Preview = getComputedPreview(entryFields.col8, entryFields.col10);

  return (
    <div className="container">
      <h1>Мастер ввода</h1>
      <div className="steps">
        <button className={step === 1 ? "active" : ""} onClick={() => setStep(1)}>1. Общие данные</button>
        <button className={step === 2 ? "active" : ""} onClick={() => setStep(2)} disabled={!documentId}>
          2. Универсальная строка
        </button>
        <button className={step === 3 ? "active" : ""} onClick={() => setStep(3)} disabled={!documentId}>
          3. Предпросмотр и проверка
        </button>
      </div>

      {message && <p className="ok">{message}</p>}
      {error && <p className="error">{error}</p>}
      {loading && <p>Загрузка...</p>}

      {step === 1 && (
        <section className="card">
          <h2>Общие данные</h2>
          <div className="lookup-row">
            <input
              placeholder="Загрузить существующий документ по UUID"
              value={loadDocumentId}
              onChange={(e) => setLoadDocumentId(e.target.value)}
            />
            <button onClick={() => void loadDocumentById()}>Загрузить</button>
          </div>
          <div className="grid">
            <label className="common-grid-span">
              Наименование, шифр товара, работы, услуги
              <input
                value={common.truNameAndCode}
                onChange={(e) => {
                  setCommon({ ...common, truNameAndCode: e.target.value });
                  setDirty(true);
                }}
              />
            </label>
            {renderCommonInput("Этап", "stage", common, setCommon, setDirty)}
            {renderCommonInput("Отчетный год", "reportYear", common, setCommon, setDirty)}
            {renderCommonInput("Планируемый год", "planYear", common, setCommon, setDirty)}
          </div>
          <div className="actions">
            <button onClick={() => void saveCommon(false)}>Сохранить черновик</button>
            <button onClick={() => void saveCommon(true)}>Далее</button>
          </div>
        </section>
      )}

      {step === 2 && (
        <section className="card">
          <h2>Универсальная строка</h2>
          <p className="hint">
            Данные в Excel берутся с сервера: после ввода нажмите «Добавить строку» или «Сохранить изменения». Пока строка не
            сохранена, она не попадает в выгрузку.
          </p>
          {!dictionaries ? (
            <p>Загрузка справочников...</p>
          ) : (
            <>
              <label>
                Раздел
                <select
                  value={sectionKey}
                  onChange={(e) => {
                    setSectionKey(e.target.value);
                    setDirty(true);
                  }}
                >
                  <option value="">Выберите раздел</option>
                  {visibleSectionKeys.map((s) => (
                    <option key={s.key} value={s.key}>
                      {s.label}
                    </option>
                  ))}
                </select>
              </label>

              <div className="meta">
                <span>Форма: {selectedSection?.formNo ?? "-"}</span>
                <span>Раздел формы: {selectedSection?.sectionNo ?? "-"}</span>
                <span>Будущий номер строки: {entries.length + 1}</span>
              </div>

              <div className="grid">
                {editableColumns.map((column) => (
                  <label key={column}>
                    {columnLabels[column] ?? column}
                    {column === "col13_2" ? (
                      <select
                        value={String(entryFields[column] ?? "")}
                        onChange={(e) => {
                          setEntryFields((prev) => ({ ...prev, [column]: e.target.value }));
                          setDirty(true);
                        }}
                      >
                        <option value="">Выберите значение</option>
                        {col13_2Options.map((value) => (
                          <option key={value} value={value}>
                            {value}
                          </option>
                        ))}
                      </select>
                    ) : (
                      <input
                        value={String(entryFields[column] ?? "")}
                        onChange={(e) => {
                          const value = numericColumns.has(column)
                            ? sanitizeDecimalInput(e.target.value, twoDecimalColumns.has(column) ? 2 : undefined)
                            : e.target.value;
                          setEntryFields((prev) => ({ ...prev, [column]: value }));
                          setDirty(true);
                        }}
                      />
                    )}
                  </label>
                ))}
                <label>
                  {columnLabels.col11} (readonly)
                  <input value={col11Preview} readOnly />
                </label>
                <label>
                  {columnLabels.col12} (readonly)
                  <input value={col12Preview} readOnly />
                </label>
              </div>

              <div className="actions">
                <button onClick={() => void submitEntry()}>{editingId ? "Сохранить изменения" : "Добавить строку"}</button>
                <button onClick={() => setStep(3)} disabled={!documentId}>
                  Перейти к проверке
                </button>
                {editingId && (
                  <button
                    onClick={() => {
                      setEditingId(null);
                      setEntryFields({});
                      setSectionKey("");
                    }}
                  >
                    Отмена
                  </button>
                )}
              </div>

              <h3>Введенные строки</h3>
              <div className="filters">
                <select value={filters.sectionKey} onChange={(e) => setFilters((p) => ({ ...p, sectionKey: e.target.value }))}>
                  <option value="">Все разделы</option>
                  {visibleSectionKeys.map((s) => (
                    <option key={s.key} value={s.key}>
                      {s.label}
                    </option>
                  ))}
                </select>
                <select value={filters.derivedFormNo} onChange={(e) => setFilters((p) => ({ ...p, derivedFormNo: e.target.value }))}>
                  <option value="">Все формы</option>
                  <option value="4">Форма 4</option>
                  <option value="5">Форма 5</option>
                  <option value="6">Форма 6</option>
                </select>
                <select value={filters.status} onChange={(e) => setFilters((p) => ({ ...p, status: e.target.value }))}>
                  <option value="">Все статусы</option>
                  <option value="valid">Валидные</option>
                  <option value="invalid">С ошибками</option>
                </select>
              </div>

              {entries.length === 0 ? (
                <p>Пока нет строк.</p>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>Раздел</th>
                      <th>Форма</th>
                      <th>Номер строки</th>
                      <th>Наименование</th>
                      <th>Статус</th>
                      <th>Действия</th>
                    </tr>
                  </thead>
                  <tbody>
                    {entries.map((entry) => (
                      <tr key={entry.entryId}>
                        <td>{dictionaries.sectionKeys.find((s) => s.key === entry.sectionKey)?.label ?? entry.sectionKey}</td>
                        <td>{entry.formNo}</td>
                        <td>{formatEntryRowNo(entry)}</td>
                        <td>{formatDisplay(entry.fields.col2)}</td>
                        <td>{mapValidationLabel(entry.validationStatus)}</td>
                        <td>
                          <button onClick={() => onEdit(entry)}>Редактировать</button>
                          <button onClick={() => onDuplicate(entry)}>Дублировать</button>
                          <button onClick={() => void removeEntry(entry.entryId)}>Удалить</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </>
          )}
        </section>
      )}

      {step === 3 && (
        <section className="card">
          <h2>Предпросмотр агрегатов и проверка</h2>
          <p className="hint">
            Экспорт строится на бэкенде из сохранённого документа (общие данные с шага 1 и строки с шага 2). Если в таблице
            ниже пусто, сначала сохраните строки на шаге 2 и нажмите «Обновить агрегаты». На бэкенде должен быть
            актуальный шаблон .xlsx с листами «Форма 4», «Форма 5», «Форма 6».
          </p>
          {!totals ? (
            <p>Загрузка агрегатов...</p>
          ) : (
            <>
              <div className="totals-grid">
                <div className="totals-card">
                  <h3>Форма 4</h3>
                  <p>Раздел 1: {formatRowCount(entryCounts.form4.section1)}</p>
                  <p>Раздел 2: {formatRowCount(entryCounts.form4.section2)}</p>
                </div>
                <div className="totals-card">
                  <h3>Форма 5</h3>
                  <p>Раздел 1: {formatRowCount(entryCounts.form5.section1)}</p>
                </div>
                <div className="totals-card">
                  <h3>Форма 6</h3>
                  <p>Итого: {formatRowCount(entryCounts.form6.total)}</p>
                </div>
              </div>

              <h3>Ошибки валидации</h3>
              {entries.length === 0 ? (
                <p>Пустой preview: строки ввода отсутствуют.</p>
              ) : validationErrors.length === 0 ? (
                <p className="ok">Ошибок нет, данные готовы к экспорту.</p>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>Раздел</th>
                      <th>Форма</th>
                      <th>Строка</th>
                      <th>Статус</th>
                      <th>Действие</th>
                    </tr>
                  </thead>
                  <tbody>
                    {validationErrors.map((entry) => (
                      <tr key={entry.entryId}>
                        <td>{dictionaries?.sectionKeys.find((s) => s.key === entry.sectionKey)?.label ?? entry.sectionKey}</td>
                        <td>{entry.formNo}</td>
                        <td>{formatEntryRowNo(entry)}</td>
                        <td>{mapValidationLabel(entry.validationStatus)}</td>
                        <td>
                          <button onClick={() => jumpToEntry(entry)}>Перейти к строке</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}

              <div className="actions">
                <button onClick={() => setStep(2)}>Назад к вводу</button>
                <button onClick={() => void loadPreviewData()} disabled={loading}>
                  Обновить агрегаты
                </button>
                <button onClick={() => void handleExport()} disabled={exporting || !documentId || !previewReadyForExport}>
                  {exporting ? "Формирование..." : "Сформировать Excel"}
                </button>
              </div>
            </>
          )}
        </section>
      )}
    </div>
  );
}

function renderCommonInput(
  label: string,
  key: keyof Omit<CommonInfoForm, "truNameAndCode">,
  common: CommonInfoForm,
  setCommon: (next: CommonInfoForm) => void,
  setDirty: (dirty: boolean) => void
) {
  return (
    <label>
      {label}
      <input
        value={String(common[key] ?? "")}
        onChange={(e) => {
          const raw = e.target.value;
          const numeric = key === "reportYear" || key === "planYear";
          setCommon({
            ...common,
            [key]: numeric ? toNumericInput(raw) : raw
          });
          setDirty(true);
        }}
      />
    </label>
  );
}

function toNumericInput(value: string): number | "" {
  if (!value.trim()) return "";
  const parsed = Number(value);
  return Number.isNaN(parsed) ? "" : parsed;
}

function sanitizeDecimalInput(raw: string, maxFractionDigits?: number): string {
  let s = raw.replace(/,/g, ".").trim();
  if (s === "") return "";
  const neg = s.startsWith("-");
  if (neg) s = s.slice(1);
  let dotSeen = false;
  let fractionDigits = 0;
  let out = "";
  for (const ch of s) {
    if (ch >= "0" && ch <= "9") {
      if (dotSeen && maxFractionDigits !== undefined && fractionDigits >= maxFractionDigits) {
        continue;
      }
      if (dotSeen) fractionDigits += 1;
      out += ch;
    } else if (ch === "." && !dotSeen) {
      dotSeen = true;
      out += ".";
    }
  }
  const result = neg ? "-" + out : out;
  if (result === "-" || result === "-.") return result;
  return result;
}

function parseDecimal(value: string | number | null | undefined): number | null {
  if (value === null || value === undefined || value === "") return null;
  if (typeof value === "number") return Number.isFinite(value) ? value : null;
  const s = String(value).trim().replace(/,/g, ".");
  if (s === "" || s === "-" || s === "." || s === "-.") return null;
  const n = Number(s);
  return Number.isNaN(n) ? null : n;
}

function isCommonValid(common: CommonInfoForm): boolean {
  return Boolean(
    common.truNameAndCode.trim() &&
      common.stage &&
      typeof common.reportYear === "number" &&
      typeof common.planYear === "number" &&
      String(common.reportYear).length === 4 &&
      String(common.planYear).length === 4
  );
}

function normalizeCommon(common: CommonInfoForm): CommonInfoApiPayload {
  const nameAndCode = common.truNameAndCode.trim();
  return {
    truName: nameAndCode,
    truCode: nameAndCode,
    stage: common.stage.trim(),
    reportYear: Number(common.reportYear),
    planYear: Number(common.planYear)
  };
}

function commonFormFromApi(common: StoredDocumentCommonInfo): CommonInfoForm {
  const n = String(common.truName ?? "").trim();
  const code = String(common.truCode ?? "").trim();
  const truNameAndCode = n && code ? `${n}, ${code}` : n || code;
  return {
    truNameAndCode,
    stage: common.stage,
    reportYear: common.reportYear,
    planYear: common.planYear
  };
}

function normalizeFields(fields: EntryFields): EntryFields {
  const normalized: EntryFields = {};
  for (const [key, value] of Object.entries(fields)) {
    if (numericColumns.has(key)) {
      const n = parseDecimal(value as string | number | null);
      if (n === null) continue;
      if (n === 0) continue;
      normalized[key] = twoDecimalColumns.has(key) ? roundTwoDecimals(n) : n;
      continue;
    }
    if (placeholdersForDash.has(key) && (value === "" || value === 0 || value === "0")) {
      normalized[key] = "-";
      continue;
    }
    if (value !== "" && value !== null && value !== undefined) {
      normalized[key] = value;
    }
  }
  return normalized;
}

function formatDisplay(value: string | number | null | undefined): string {
  if (value === null || value === undefined || value === "") return "-";
  return String(value);
}

function mapValidationLabel(status: string): string {
  if (status === "valid") return "Валидно";
  if (status === "invalid") return "С ошибками";
  return status || "-";
}

function formatEntryRowNo(entry: Pick<EntryItem, "formNo" | "rowNo">): string {
  if (entry.formNo === 6) {
    const dotIndex = entry.rowNo.indexOf(".");
    return dotIndex >= 0 ? entry.rowNo.slice(dotIndex + 1) : entry.rowNo;
  }
  return entry.rowNo;
}

function formatRowCount(count: number): string {
  const mod10 = count % 10;
  const mod100 = count % 100;
  if (mod100 >= 11 && mod100 <= 14) {
    return `${count} строк`;
  }
  if (mod10 === 1) {
    return `${count} строка`;
  }
  if (mod10 >= 2 && mod10 <= 4) {
    return `${count} строки`;
  }
  return `${count} строк`;
}

function roundTwoDecimals(value: number): number {
  return Math.round(value * 100) / 100;
}

function formatTwoDecimals(value: number): string {
  return roundTwoDecimals(value).toFixed(2);
}

function getComputedPreview(left: string | number | null | undefined, right: string | number | null | undefined): string {
  const a = parseDecimal(left);
  const b = parseDecimal(right);
  if (a === null || b === null) return "";
  const product = a * b;
  return Number.isFinite(product) ? formatTwoDecimals(product) : "";
}

function isValidSupplierInn(value: string | number | null | undefined): boolean {
  if (value === null || value === undefined || value === "") return true;
  const s = String(value).trim();
  if (!s) return true;
  return /^[0-9]{10}([0-9]{2})?$/.test(s);
}

function toMessage(error: unknown): string {
  if (error instanceof Error) return error.message;
  return "Неизвестная ошибка";
}

export default App;
