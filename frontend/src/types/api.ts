export type CommonInfoForm = {
  truNameAndCode: string;
  stage: string;
  reportYear: number | "";
  planYear: number | "";
};

export type CommonInfoApiPayload = {
  truName: string;
  truCode?: string;
  stage: string;
  reportYear: number;
  planYear: number;
};

export type StoredDocumentCommonInfo = CommonInfoApiPayload & {
  truCode?: string | null;
};

export type SectionKeyItem = {
  key: string;
  label: string;
  formNo: number;
  sectionNo: number;
};

export type DictionaryItem = {
  code: string;
  label: string;
};

export type DictionariesResponse = {
  sectionKeys: SectionKeyItem[];
  col13_2Values: DictionaryItem[];
  col5_2Values: DictionaryItem[];
};

export type EntryFields = Record<string, string | number | null>;

export type EntryItem = {
  entryId: string;
  sectionKey: string;
  formNo: number;
  sectionNo: number;
  rowNo: string;
  fields: EntryFields;
  computed: { col11?: number; col12?: number };
  validationStatus: string;
};

export type ListEntriesResponse = {
  items: EntryItem[];
  total: number;
};

export type UpsertEntryRequest = {
  sectionKey: string;
  fields: EntryFields;
};

export type PairTotal = {
  col11: number;
  col12: number;
};

export type TotalsResponse = {
  form4: {
    section1Total: PairTotal;
    section2Total: PairTotal;
    section1And2Total: PairTotal;
  };
  form5: {
    section1Total: PairTotal;
  };
  form6: {
    total: PairTotal;
  };
};

export type ExportResponse = {
  downloadUrl: string;
  expiresAt: string;
  exportMeta: {
    templatePreserved: boolean;
    updatedSheets: string[];
  };
};

export type ReferenceSupplier = {
  id: string;
  name: string;
  inn: string | null;
};

export type ReferenceMaterial = {
  id: string;
  name: string;
  okpdCode: string | null;
  ekpsCode: string | null;
  fnn: string | null;
};

export type ReferenceSearchResponse<T> = {
  items: T[];
};
