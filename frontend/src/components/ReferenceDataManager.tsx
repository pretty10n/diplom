import { useCallback, useEffect, useState } from "react";
import {
  createMaterial,
  createSupplier,
  deleteMaterial,
  deleteSupplier,
  listMaterials,
  listSuppliers,
  updateMaterial,
  updateSupplier
} from "../api/client";
import type { ReferenceMaterial, ReferenceSupplier } from "../types/api";

type ReferenceDataManagerProps = {
  onClose: () => void;
};

type Tab = "suppliers" | "materials";

const emptySupplier = { name: "", inn: "" };
const emptyMaterial = { name: "", okpdCode: "", ekpsCode: "", fnn: "" };

export function ReferenceDataManager({ onClose }: ReferenceDataManagerProps) {
  const [tab, setTab] = useState<Tab>("suppliers");
  const [suppliers, setSuppliers] = useState<ReferenceSupplier[]>([]);
  const [materials, setMaterials] = useState<ReferenceMaterial[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [editingSupplierId, setEditingSupplierId] = useState<string | null>(null);
  const [editingMaterialId, setEditingMaterialId] = useState<string | null>(null);
  const [supplierForm, setSupplierForm] = useState(emptySupplier);
  const [materialForm, setMaterialForm] = useState(emptyMaterial);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const [supplierResponse, materialResponse] = await Promise.all([listSuppliers(), listMaterials()]);
      setSuppliers(supplierResponse.items);
      setMaterials(materialResponse.items);
    } catch (e) {
      setError(toMessage(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadData();
  }, [loadData]);

  async function submitSupplier() {
    if (!supplierForm.name.trim()) {
      setError("Укажите наименование поставщика.");
      return;
    }
    if (!isValidSupplierInn(supplierForm.inn)) {
      setError("ИНН: допустимы только 10 или 12 цифр (или оставьте поле пустым).");
      return;
    }
    setLoading(true);
    setError("");
    setMessage("");
    try {
      const payload = {
        name: supplierForm.name.trim(),
        inn: supplierForm.inn.trim() || undefined
      };
      if (editingSupplierId) {
        await updateSupplier(editingSupplierId, payload);
        setMessage("Поставщик обновлён.");
      } else {
        await createSupplier(payload);
        setMessage("Поставщик добавлен.");
      }
      setSupplierForm(emptySupplier);
      setEditingSupplierId(null);
      await loadData();
    } catch (e) {
      setError(toMessage(e));
    } finally {
      setLoading(false);
    }
  }

  async function submitMaterial() {
    if (!materialForm.name.trim()) {
      setError("Укажите наименование материала.");
      return;
    }
    setLoading(true);
    setError("");
    setMessage("");
    try {
      const payload = {
        name: materialForm.name.trim(),
        okpdCode: materialForm.okpdCode.trim() || undefined,
        ekpsCode: materialForm.ekpsCode.trim() || undefined,
        fnn: materialForm.fnn.trim() || undefined
      };
      if (editingMaterialId) {
        await updateMaterial(editingMaterialId, payload);
        setMessage("Материал обновлён.");
      } else {
        await createMaterial(payload);
        setMessage("Материал добавлен.");
      }
      setMaterialForm(emptyMaterial);
      setEditingMaterialId(null);
      await loadData();
    } catch (e) {
      setError(toMessage(e));
    } finally {
      setLoading(false);
    }
  }

  async function removeSupplier(id: string) {
    if (!window.confirm("Удалить поставщика из справочника?")) return;
    setLoading(true);
    setError("");
    try {
      await deleteSupplier(id);
      setMessage("Поставщик удалён.");
      if (editingSupplierId === id) {
        setEditingSupplierId(null);
        setSupplierForm(emptySupplier);
      }
      await loadData();
    } catch (e) {
      setError(toMessage(e));
    } finally {
      setLoading(false);
    }
  }

  async function removeMaterial(id: string) {
    if (!window.confirm("Удалить материал из справочника?")) return;
    setLoading(true);
    setError("");
    try {
      await deleteMaterial(id);
      setMessage("Материал удалён.");
      if (editingMaterialId === id) {
        setEditingMaterialId(null);
        setMaterialForm(emptyMaterial);
      }
      await loadData();
    } catch (e) {
      setError(toMessage(e));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="reference-data-title">
      <div className="modal-card reference-modal">
        <div className="modal-header">
          <h2 id="reference-data-title">Справочники</h2>
          <button type="button" onClick={onClose}>
            Закрыть
          </button>
        </div>

        <div className="tabs">
          <button type="button" className={tab === "suppliers" ? "active" : ""} onClick={() => setTab("suppliers")}>
            Поставщики
          </button>
          <button type="button" className={tab === "materials" ? "active" : ""} onClick={() => setTab("materials")}>
            Материалы
          </button>
        </div>

        {message && <p className="ok">{message}</p>}
        {error && <p className="error">{error}</p>}
        {loading && <p>Загрузка...</p>}

        {tab === "suppliers" && (
          <>
            <div className="grid">
              <label>
                Наименование организации-поставщика
                <input
                  value={supplierForm.name}
                  onChange={(e) => setSupplierForm((prev) => ({ ...prev, name: e.target.value }))}
                />
              </label>
              <label>
                ИНН
                <input
                  value={supplierForm.inn}
                  onChange={(e) => setSupplierForm((prev) => ({ ...prev, inn: e.target.value }))}
                />
              </label>
            </div>
            <div className="actions">
              <button type="button" onClick={() => void submitSupplier()}>
                {editingSupplierId ? "Сохранить изменения" : "Добавить"}
              </button>
              {editingSupplierId && (
                <button
                  type="button"
                  onClick={() => {
                    setEditingSupplierId(null);
                    setSupplierForm(emptySupplier);
                  }}
                >
                  Отмена
                </button>
              )}
            </div>
            <table>
              <thead>
                <tr>
                  <th>Наименование</th>
                  <th>ИНН</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {suppliers.length === 0 ? (
                  <tr>
                    <td colSpan={3}>Справочник пуст.</td>
                  </tr>
                ) : (
                  suppliers.map((item) => (
                    <tr key={item.id}>
                      <td>{item.name}</td>
                      <td>{item.inn ?? "-"}</td>
                      <td>
                        <button
                          type="button"
                          onClick={() => {
                            setEditingSupplierId(item.id);
                            setSupplierForm({ name: item.name, inn: item.inn ?? "" });
                          }}
                        >
                          Редактировать
                        </button>
                        <button type="button" onClick={() => void removeSupplier(item.id)}>
                          Удалить
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </>
        )}

        {tab === "materials" && (
          <>
            <div className="grid">
              <label>
                Наименование
                <input
                  value={materialForm.name}
                  onChange={(e) => setMaterialForm((prev) => ({ ...prev, name: e.target.value }))}
                />
              </label>
              <label>
                Код ОКП/ОКПД2
                <input
                  value={materialForm.okpdCode}
                  onChange={(e) => setMaterialForm((prev) => ({ ...prev, okpdCode: e.target.value }))}
                />
              </label>
              <label>
                Код ЕКПС
                <input
                  value={materialForm.ekpsCode}
                  onChange={(e) => setMaterialForm((prev) => ({ ...prev, ekpsCode: e.target.value }))}
                />
              </label>
              <label>
                ФНН
                <input value={materialForm.fnn} onChange={(e) => setMaterialForm((prev) => ({ ...prev, fnn: e.target.value }))} />
              </label>
            </div>
            <div className="actions">
              <button type="button" onClick={() => void submitMaterial()}>
                {editingMaterialId ? "Сохранить изменения" : "Добавить"}
              </button>
              {editingMaterialId && (
                <button
                  type="button"
                  onClick={() => {
                    setEditingMaterialId(null);
                    setMaterialForm(emptyMaterial);
                  }}
                >
                  Отмена
                </button>
              )}
            </div>
            <table>
              <thead>
                <tr>
                  <th>Наименование</th>
                  <th>ОКПД</th>
                  <th>ЕКПС</th>
                  <th>ФНН</th>
                  <th>Действия</th>
                </tr>
              </thead>
              <tbody>
                {materials.length === 0 ? (
                  <tr>
                    <td colSpan={5}>Справочник пуст.</td>
                  </tr>
                ) : (
                  materials.map((item) => (
                    <tr key={item.id}>
                      <td>{item.name}</td>
                      <td>{item.okpdCode ?? "-"}</td>
                      <td>{item.ekpsCode ?? "-"}</td>
                      <td>{item.fnn ?? "-"}</td>
                      <td>
                        <button
                          type="button"
                          onClick={() => {
                            setEditingMaterialId(item.id);
                            setMaterialForm({
                              name: item.name,
                              okpdCode: item.okpdCode ?? "",
                              ekpsCode: item.ekpsCode ?? "",
                              fnn: item.fnn ?? ""
                            });
                          }}
                        >
                          Редактировать
                        </button>
                        <button type="button" onClick={() => void removeMaterial(item.id)}>
                          Удалить
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </>
        )}
      </div>
    </div>
  );
}

function isValidSupplierInn(value: string): boolean {
  const s = value.trim();
  if (!s) return true;
  return /^[0-9]{10}([0-9]{2})?$/.test(s);
}

function toMessage(error: unknown): string {
  if (error instanceof Error) return error.message;
  return "Неизвестная ошибка";
}
