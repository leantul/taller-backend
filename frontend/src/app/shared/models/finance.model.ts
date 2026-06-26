export interface FinanceRow {
  repairId: string;
  clientName: string;
  date: string | null;
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
  zeroFinalAmountCount: number;
  positiveFinalAmountCount: number;
  netIncome: number | string;
  averageNet: number | string;
  deliveredCount: number;
  monthlyNet: { label: string; value: number | string }[];
  rows: FinanceRow[];
}
