import { useEffect, useId, useRef, useState } from "react";

type AutocompleteInputProps<T> = {
  label: string;
  value: string;
  onChange: (value: string) => void;
  onSelect: (item: T) => void;
  search: (query: string) => Promise<T[]>;
  getKey: (item: T) => string;
  getLabel: (item: T) => string;
  getHint?: (item: T) => string;
  minChars?: number;
  placeholder?: string;
};

export function AutocompleteInput<T>({
  label,
  value,
  onChange,
  onSelect,
  search,
  getKey,
  getLabel,
  getHint,
  minChars = 2,
  placeholder
}: AutocompleteInputProps<T>) {
  const listId = useId();
  const rootRef = useRef<HTMLDivElement>(null);
  const [suggestions, setSuggestions] = useState<T[]>([]);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [activeIndex, setActiveIndex] = useState(-1);

  useEffect(() => {
    const trimmed = value.trim();
    if (trimmed.length < minChars) {
      setSuggestions([]);
      setOpen(false);
      setActiveIndex(-1);
      return;
    }

    const timer = window.setTimeout(() => {
      setLoading(true);
      void search(trimmed)
        .then((items) => {
          setSuggestions(items);
          setOpen(items.length > 0);
          setActiveIndex(-1);
        })
        .catch(() => {
          setSuggestions([]);
          setOpen(false);
        })
        .finally(() => setLoading(false));
    }, 250);

    return () => window.clearTimeout(timer);
  }, [value, minChars, search]);

  useEffect(() => {
    const onPointerDown = (event: MouseEvent) => {
      if (!rootRef.current?.contains(event.target as Node)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", onPointerDown);
    return () => document.removeEventListener("mousedown", onPointerDown);
  }, []);

  function pick(item: T) {
    onSelect(item);
    setOpen(false);
    setSuggestions([]);
    setActiveIndex(-1);
  }

  return (
    <label className="autocomplete">
      {label}
      <div className="autocomplete-field" ref={rootRef}>
      <input
        value={value}
        placeholder={placeholder}
        onChange={(e) => onChange(e.target.value)}
        onFocus={() => {
          if (suggestions.length > 0) setOpen(true);
        }}
        onKeyDown={(e) => {
          if (!open || suggestions.length === 0) return;
          if (e.key === "ArrowDown") {
            e.preventDefault();
            setActiveIndex((prev) => (prev + 1) % suggestions.length);
          } else if (e.key === "ArrowUp") {
            e.preventDefault();
            setActiveIndex((prev) => (prev <= 0 ? suggestions.length - 1 : prev - 1));
          } else if (e.key === "Enter" && activeIndex >= 0) {
            e.preventDefault();
            pick(suggestions[activeIndex]);
          } else if (e.key === "Escape") {
            setOpen(false);
          }
        }}
        role="combobox"
        aria-expanded={open}
        aria-controls={listId}
        aria-autocomplete="list"
      />
      {loading && <span className="autocomplete-status">Поиск...</span>}
      {open && suggestions.length > 0 && (
        <ul className="autocomplete-list" id={listId} role="listbox">
          {suggestions.map((item, index) => (
            <li
              key={getKey(item)}
              role="option"
              aria-selected={index === activeIndex}
              className={index === activeIndex ? "active" : ""}
              onMouseDown={(e) => e.preventDefault()}
              onClick={() => pick(item)}
            >
              <span>{getLabel(item)}</span>
              {getHint && <span className="autocomplete-hint">{getHint(item)}</span>}
            </li>
          ))}
        </ul>
      )}
      </div>
    </label>
  );
}
