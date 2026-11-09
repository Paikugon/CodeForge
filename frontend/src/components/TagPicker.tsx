'use client';
import { useEffect, useId, useRef, useState } from 'react';
import { Check, ChevronDown, Search, Tags, X } from 'lucide-react';
import type { Tag } from '../types/tag';

export default function TagPicker({
  tags,
  selected,
  onChange,
  label = 'Tags',
  compact = false,
}: {
  tags: Tag[];
  selected: number[];
  onChange: (ids: number[]) => void;
  label?: string;
  compact?: boolean;
}) {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');
  const root = useRef<HTMLDivElement>(null);
  const panelId = useId();

  useEffect(() => {
    if (!open) return;
    const close = (event: MouseEvent) => {
      if (!root.current?.contains(event.target as Node)) setOpen(false);
    };
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, [open]);

  const selectedTags = [...new Set(selected)].map(
    id => tags.find(tag => tag.id === id) ?? { id, name: `Unknown tag #${id}` },
  );
  const shown = [...tags]
    .sort((a, b) => a.name.localeCompare(b.name))
    .filter(tag =>
      tag.name.toLocaleLowerCase().includes(search.toLocaleLowerCase()),
    );
  const toggle = (id: number) =>
    onChange(
      selected.includes(id)
        ? selected.filter(value => value !== id)
        : [...selected, id],
    );

  return (
    <div
      className={`tag-picker ${compact ? 'compact-picker' : ''}`}
      ref={root}
      onKeyDown={event => {
        if (event.key === 'Escape') {
          event.stopPropagation();
          setOpen(false);
          root.current
            ?.querySelector<HTMLButtonElement>('.tag-picker-trigger')
            ?.focus();
        }
      }}
    >
      <div
        className={`tag-picker-control ${selectedTags.length ? 'has-selection' : ''}`}
        onClick={() => setOpen(value => !value)}
      >
        <button
          type="button"
          className="tag-picker-trigger"
          aria-label={label}
          aria-haspopup="dialog"
          aria-expanded={open}
          aria-controls={open ? panelId : undefined}
          onClick={event => {
            event.stopPropagation();
            setOpen(value => !value);
          }}
          onKeyDown={event => {
            if (event.key === 'ArrowDown') {
              event.preventDefault();
              setOpen(true);
            }
          }}
        >
          <span className="sr-only">
            {label}
            {selectedTags.length ? `: ${selectedTags.length} selected` : ''}
          </span>
        </button>

        <Tags className="tag-picker-icon" size={15} aria-hidden="true" />
        <div
          className={`tag-picker-values ${selectedTags.length ? '' : 'is-empty'}`}
          title={selectedTags.map(tag => tag.name).join(', ')}
        >
          {selectedTags.length ? (
            selectedTags.map(tag => (
              <span className="tag-picker-chip" key={tag.id}>
                <span>{tag.name}</span>
                <button
                  type="button"
                  aria-label={`Remove ${tag.name} ${compact ? 'filter' : 'tag'}`}
                  onClick={event => {
                    event.stopPropagation();
                    toggle(tag.id);
                  }}
                >
                  <X size={11} />
                </button>
              </span>
            ))
          ) : (
            <span className="tag-picker-placeholder">
              {compact ? 'All tags' : 'Choose tags'}
            </span>
          )}
        </div>
        {selectedTags.length > 0 && (
          <span
            className="tag-picker-count"
            aria-label={`${selectedTags.length} tags selected`}
          >
            {selectedTags.length}
          </span>
        )}
        <ChevronDown
          className={`tag-picker-chevron ${open ? 'is-open' : ''}`}
          size={14}
          aria-hidden="true"
        />
      </div>
      {open && (
        <div
          id={panelId}
          role="dialog"
          aria-label={`${label} choices`}
          className="tag-picker-panel"
        >
          <div className="tag-picker-heading">
            <strong>{label}</strong>
            <span>{compact ? 'Match all selected' : 'Select concepts'}</span>
          </div>
          <label className="tag-search">
            <Search size={15} />
            <input
              autoFocus
              placeholder="Find a tag…"
              aria-label="Search tags"
              onKeyDown={event => {
                if (event.key === 'Enter') event.preventDefault();
              }}
              value={search}
              onChange={event => setSearch(event.target.value)}
            />
          </label>
          <div className="tag-options">
            {shown.map(tag => (
              <button
                type="button"
                role="checkbox"
                aria-checked={selected.includes(tag.id)}
                key={tag.id}
                className={`tag-option ${selected.includes(tag.id) ? 'is-selected' : ''}`}
                onClick={() => toggle(tag.id)}
              >
                <span className="tag-option-check" aria-hidden="true">
                  {selected.includes(tag.id) && (
                    <Check size={12} strokeWidth={2.5} />
                  )}
                </span>
                <span className="tag-option-name">{tag.name}</span>
              </button>
            ))}
            {!shown.length && (
              <p className="tag-options-empty">No matching tags.</p>
            )}
          </div>
          <div className="tag-picker-footer">
            <button
              type="button"
              className="text-button"
              disabled={!selected.length}
              onClick={() => onChange([])}
            >
              Clear selection
            </button>
            <button
              type="button"
              className="button compact"
              onClick={() => setOpen(false)}
            >
              Done
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
