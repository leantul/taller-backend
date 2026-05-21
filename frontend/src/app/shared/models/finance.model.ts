import { Repair } from './repair.model';

export interface FinanceRow {
  repairId: string;
  orderNumber: string;
  date: string | null;
  status: Repair['status'];
  income: number | string;
  partsCost: number | string;
  net: number | string;
}

export interface FinanceSummary {
  from: string | null;
  to: string | null;
  repairCount: number;
  totalIncome: number | string;
  totalPartsCost: number | string;
  totalLabor: number | string;
  totalQuoted: number | string;
  netIncome: number | string;
  averageNet: number | string;
  deliveredCount: number;
  rows: FinanceRow[];
}
