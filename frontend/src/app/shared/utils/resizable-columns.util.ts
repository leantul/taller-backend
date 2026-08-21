export type ResizableColumn<K extends string> = {
  key: K;
  width: string;
};

export function resolveColumnWidth<K extends string>(columns: ReadonlyArray<ResizableColumn<K>>, columnKey: K): string {
  return columns.find((column) => column.key === columnKey)?.width || 'auto';
}

export function restoreColumnWidths<K extends string>(
  columns: ReadonlyArray<ResizableColumn<K>>,
  storageKey: string
): void {
  const stored = localStorage.getItem(storageKey);
  if (!stored) return;

  try {
    const widths = JSON.parse(stored) as Partial<Record<K, string>>;
    columns.forEach((column) => {
      if (widths[column.key]) column.width = widths[column.key]!;
    });
  } catch {
    localStorage.removeItem(storageKey);
  }
}

export function persistColumnWidths<K extends string>(
  columns: ReadonlyArray<ResizableColumn<K>>,
  storageKey: string
): void {
  localStorage.setItem(
    storageKey,
    JSON.stringify(Object.fromEntries(columns.map((column) => [column.key, column.width])))
  );
}

export function beginColumnResize<K extends string>(
  event: MouseEvent,
  columnKey: K,
  columns: ReadonlyArray<ResizableColumn<K>>,
  onWidthChange: () => void
): void {
  event.preventDefault();
  event.stopPropagation();
  const header = (event.currentTarget as HTMLElement).closest('th');
  if (!header) return;

  const resizeStartX = event.clientX;
  const resizeStartWidth = header.getBoundingClientRect().width;

  const onMouseMove = (moveEvent: MouseEvent) => {
    const nextWidth = Math.max(96, Math.round(resizeStartWidth + (moveEvent.clientX - resizeStartX)));
    const column = columns.find((item) => item.key === columnKey);
    if (column) {
      column.width = `${nextWidth}px`;
      onWidthChange();
    }
  };

  const onMouseUp = () => {
    window.removeEventListener('mousemove', onMouseMove);
    window.removeEventListener('mouseup', onMouseUp);
  };

  window.addEventListener('mousemove', onMouseMove);
  window.addEventListener('mouseup', onMouseUp);
}
