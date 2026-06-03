import type {
  CommonInfoApiPayload,
  DictionariesResponse,
  StoredDocumentCommonInfo,
  ExportResponse,
  ListEntriesResponse,
  TotalsResponse,
  UpsertEntryRequest
} from "../types/api";

const API_BASE = "/api/v1";

type BackendErrorEnvelope = {
  error?: {
    message?: string;
    code?: string;
    details?: Array<{ field?: string; reason?: string }>;
  };
};

export function describeBackendApiError(raw: string): string {
  const trimmed = raw.trim();
  if (!trimmed) return "Ошибка API";
  try {
    const parsed = JSON.parse(trimmed) as BackendErrorEnvelope;
    const body = parsed.error;
    if (!body) return trimmed;
    const detailLines = (body.details ?? [])
      .map((d) => {
        const prefix = d.field?.trim() ? `${d.field?.trim()}: ` : "";
        const reason = d.reason?.trim() ?? "";
        return `${prefix}${reason}`.trim();
      })
      .filter(Boolean);
    const headline = body.message?.trim() ?? "";
    if (!headline || headline === "Validation failed") {
      if (detailLines.length > 0) return detailLines.join("; ");
    } else if (detailLines.length > 0 && !detailLines.every((line) => headline.includes(line))) {
      return `${headline}; ${detailLines.join("; ")}`;
    }
    if (headline) return headline;
    return body.code?.trim() || trimmed;
  } catch {
    return trimmed;
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...init
  });

  const raw = await response.text();

  if (!response.ok) {
    throw new Error(describeBackendApiError(raw || "Ошибка API"));
  }

  if (response.status === 204 || raw.trim().length === 0) {
    return undefined as T;
  }

  return JSON.parse(raw) as T;
}

export async function createDocument(common: CommonInfoApiPayload): Promise<{ id: string }> {
  return request<{ id: string }>("/documents", {
    method: "POST",
    body: JSON.stringify({ common })
  });
}

export async function updateCommon(documentId: string, common: CommonInfoApiPayload): Promise<void> {
  await request<void>(`/documents/${documentId}/common`, {
    method: "PATCH",
    body: JSON.stringify(common)
  });
}

export async function getDocument(
  documentId: string
): Promise<{ id: string; status: string; common: StoredDocumentCommonInfo }> {
  return request(`/documents/${documentId}`);
}

export async function getDictionaries(): Promise<DictionariesResponse> {
  return request<DictionariesResponse>("/dictionaries");
}

export async function listEntries(
  documentId: string,
  filters: { sectionKey?: string; derivedFormNo?: string; status?: string }
): Promise<ListEntriesResponse> {
  const params = new URLSearchParams();
  if (filters.sectionKey) params.set("sectionKey", filters.sectionKey);
  if (filters.derivedFormNo) params.set("derivedFormNo", filters.derivedFormNo);
  if (filters.status) params.set("status", filters.status);
  const query = params.toString();
  return request<ListEntriesResponse>(`/documents/${documentId}/entries${query ? `?${query}` : ""}`);
}

export async function addEntry(documentId: string, payload: UpsertEntryRequest): Promise<void> {
  await request(`/documents/${documentId}/entries`, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function updateEntry(documentId: string, entryId: string, payload: UpsertEntryRequest): Promise<void> {
  await request(`/documents/${documentId}/entries/${entryId}`, {
    method: "PATCH",
    body: JSON.stringify(payload)
  });
}

export async function deleteEntry(documentId: string, entryId: string): Promise<void> {
  await request(`/documents/${documentId}/entries/${entryId}`, { method: "DELETE" });
}

export async function getTotals(documentId: string): Promise<TotalsResponse> {
  return request(`/documents/${documentId}/totals`);
}

export async function exportDocument(documentId: string, fileName: string): Promise<Blob> {
  const exportResponse = await request<ExportResponse>(`/documents/${documentId}/export`, {
    method: "POST",
    body: JSON.stringify({
      format: "xlsx",
      mode: "fill_template",
      fileName
    })
  });
  const fileResponse = await fetch(exportResponse.downloadUrl);
  if (!fileResponse.ok) {
    const errText = await fileResponse.text();
    throw new Error(describeBackendApiError(errText) || "Не удалось скачать сформированный файл экспорта.");
  }
  return fileResponse.blob();
}
