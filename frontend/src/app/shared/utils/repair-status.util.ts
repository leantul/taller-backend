import { Repair } from '../models/repair.model';

export type RepairStatus = Repair['status'];

export const REPAIR_STATUS_OPTIONS: ReadonlyArray<{ label: string; value: RepairStatus }> = [
  { label: 'Por recibir', value: 'POR_RECIBIR' },
  { label: 'Recibida', value: 'RECIBIDA' },
  { label: 'Presupuestada', value: 'PRESUPUESTADA_ESPERANDO_RESPUESTA' },
  { label: 'Haciendo', value: 'HACIENDO' },
  { label: 'Esperando retiro', value: 'ESPERANDO_RETIRO' },
  { label: 'Retirada', value: 'RETIRADA' }
];

export function repairStatusLabel(status: RepairStatus): string {
  return REPAIR_STATUS_OPTIONS.find((option) => option.value === status)?.label || status;
}

export function repairStatusClass(status: RepairStatus): string {
  return {
    POR_RECIBIR: 'is-muted',
    RECIBIDA: 'is-info',
    PRESUPUESTADA_ESPERANDO_RESPUESTA: 'is-warning',
    HACIENDO: 'is-active',
    ESPERANDO_RETIRO: 'is-success',
    RETIRADA: 'is-closed'
  }[status];
}

export function toDateTimeLocal(value?: string): string {
  const date = value ? new Date(value) : new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

export function fromDateTimeLocal(value: string): string | undefined {
  return value || undefined;
}
