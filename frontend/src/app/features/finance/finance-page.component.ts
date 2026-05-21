import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { ApiService } from '../../core/services/api.service';
import { FinanceRow, FinanceSummary } from '../../shared/models/finance.model';
import { Repair } from '../../shared/models/repair.model';

@Component({
  selector: 'app-finance-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule],
  template: `
    <section class="page-heading">
      <div>
        <span class="eyebrow">Resultado</span>
        <h1>Finanzas</h1>
      </div>
      <p>Resumen de ingresos reales cobrados, costo de repuestos y ganancia neta dentro del rango de fechas elegido.</p>
    </section>

    <section class="finance-filters finance-toolbar">
      <label class="field">
        <span>Desde</span>
        <input class="control" type="date" [(ngModel)]="draftFromDate" />
      </label>
      <label class="field">
        <span>Hasta</span>
        <input class="control" type="date" [(ngModel)]="draftToDate" />
      </label>
      <button class="primary-button finance-reset" type="button" (click)="applyFilters()">
        <i class="pi pi-filter"></i>
        <span>Aplicar filtros</span>
      </button>
      <button class="secondary-button finance-reset" type="button" (click)="resetDates()">Limpiar rango</button>
    </section>

    <section class="dashboard-grid metrics-grid finance-metrics">
      <p-card styleClass="metric-card"><span class="metric-label">Reparaciones</span><div class="metric">{{ repairCount }}</div><small>Incluidas en el rango</small></p-card>
      <p-card styleClass="metric-card"><span class="metric-label">Ingresos</span><div class="metric">{{ formatMoney(totalIncome) }}</div><small>Total cobrado real</small></p-card>
      <p-card styleClass="metric-card"><span class="metric-label">Gasto en repuestos</span><div class="metric">{{ formatMoney(totalPartsCost) }}</div><small>Suma de costos</small></p-card>
      <p-card styleClass="metric-card revenue"><span class="metric-label">Ganancia neta</span><div class="metric">{{ formatMoney(netIncome) }}</div><small>Cobrado menos repuestos</small></p-card>
    </section>

    <section class="dashboard-grid lists finance-layout">
      <p-card header="Composición del periodo">
        <div class="ops-summary-grid finance-breakdown">
          <div class="ops-item"><span>Mano de obra cargada</span><strong>{{ formatMoney(totalLabor) }}</strong></div>
          <div class="ops-item"><span>Presupuestos emitidos</span><strong>{{ formatMoney(totalQuoted) }}</strong></div>
          <div class="ops-item"><span>Ordenes entregadas</span><strong>{{ deliveredCount }}</strong></div>
          <div class="ops-item"><span>Margen promedio</span><strong>{{ formatMoney(averageNet) }}</strong></div>
        </div>
      </p-card>

      <p-card header="Detalle por reparación">
        <div class="native-table-wrap">
          <table class="native-table finance-table">
            <thead><tr><th>Orden</th><th>Fecha</th><th>Estado</th><th>Ingreso</th><th>Repuestos</th><th>Neto</th></tr></thead>
            <tbody>
              @for (row of financeRows; track row.repairId + row.date) {
                <tr>
                  <td>#{{ row.orderNumber || '-' }}</td>
                  <td>{{ row.date ? (row.date | date:'dd/MM/yyyy') : '-' }}</td>
                  <td>{{ statusLabel(row.status) }}</td>
                  <td>{{ formatMoney(row.income) }}</td>
                  <td>{{ formatMoney(row.partsCost) }}</td>
                  <td>{{ formatMoney(row.net) }}</td>
                </tr>
              } @empty {
                <tr><td class="empty-cell" colspan="6">No hay reparaciones dentro del rango elegido.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </p-card>
    </section>
  `
})
export class FinancePageComponent implements OnInit {
  draftFromDate = '';
  draftToDate = '';
  financeRows: FinanceRow[] = [];
  repairCount = 0;
  totalIncome = 0;
  totalPartsCost = 0;
  totalLabor = 0;
  totalQuoted = 0;
  netIncome = 0;
  averageNet = 0;
  deliveredCount = 0;

  constructor(private readonly api: ApiService, private readonly changeDetector: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.setCurrentMonthRange();
    this.applyFilters();
  }

  applyFilters(): void {
    this.api.getFinanceSummary(this.draftFromDate || undefined, this.draftToDate || undefined).subscribe((summary) => {
      this.hydrateSummary(summary);
      this.changeDetector.detectChanges();
    });
  }

  resetDates(): void {
    this.setCurrentMonthRange();
    this.applyFilters();
  }

  formatMoney(value: unknown): string {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS',
      maximumFractionDigits: 0
    }).format(this.asMoney(value));
  }

  asMoney(value: unknown): number {
    if (typeof value === 'number') {
      return Number.isFinite(value) ? value : 0;
    }

    if (typeof value === 'string') {
      const sanitized = value.trim().replace(/\s+/g, '').replace(/[^0-9,.-]/g, '');
      if (!sanitized) {
        return 0;
      }

      let normalized = sanitized;
      if (sanitized.includes(',') && sanitized.includes('.')) {
        normalized = sanitized.lastIndexOf(',') > sanitized.lastIndexOf('.')
          ? sanitized.replace(/\./g, '').replace(',', '.')
          : sanitized.replace(/,/g, '');
      } else if (sanitized.includes(',')) {
        normalized = sanitized.replace(/\./g, '').replace(',', '.');
      }

      const parsed = Number(normalized);
      return Number.isFinite(parsed) ? parsed : 0;
    }

    if (value && typeof value === 'object') {
      const nestedValue = (value as { amount?: unknown; value?: unknown }).amount ?? (value as { value?: unknown }).value;
      return this.asMoney(nestedValue);
    }

    return 0;
  }

  statusLabel(status: Repair['status']): string {
    switch (status) {
      case 'POR_RECIBIR': return 'Por recibir';
      case 'RECIBIDA': return 'Recibida';
      case 'PRESUPUESTADA_ESPERANDO_RESPUESTA': return 'Presupuestada';
      case 'HACIENDO': return 'Haciendo';
      case 'ESPERANDO_RETIRO': return 'Esperando retiro';
      case 'RETIRADA': return 'Retirada';
      default: return status;
    }
  }

  private hydrateSummary(summary: FinanceSummary): void {
    this.financeRows = summary.rows || [];
    this.repairCount = summary.repairCount || 0;
    this.totalIncome = this.asMoney(summary.totalIncome);
    this.totalPartsCost = this.asMoney(summary.totalPartsCost);
    this.totalLabor = this.asMoney(summary.totalLabor);
    this.totalQuoted = this.asMoney(summary.totalQuoted);
    this.netIncome = this.asMoney(summary.netIncome);
    this.averageNet = this.asMoney(summary.averageNet);
    this.deliveredCount = summary.deliveredCount || 0;
  }

  private setCurrentMonthRange(): void {
    const today = new Date();
    this.draftFromDate = this.toInputDate(new Date(today.getFullYear(), today.getMonth(), 1));
    this.draftToDate = this.toInputDate(new Date(today.getFullYear(), today.getMonth() + 1, 0));
  }

  private toInputDate(value: Date): string {
    const year = value.getFullYear();
    const month = `${value.getMonth() + 1}`.padStart(2, '0');
    const day = `${value.getDate()}`.padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
