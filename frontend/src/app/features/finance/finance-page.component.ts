import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CardModule } from 'primeng/card';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/services/api.service';
import { Repair, RepairPart } from '../../shared/models/repair.model';

type FinanceRow = {
  orderNumber: string;
  dateLabel: string;
  statusLabel: string;
  income: number;
  partsCost: number;
  net: number;
};

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
      <p>Resumen de ingresos, costo de repuestos y ganancia neta dentro del rango de fechas elegido.</p>
    </section>

    <section class="finance-filters">
      <label class="field">
        <span>Desde</span>
        <input class="control" type="date" [(ngModel)]="fromDate" (ngModelChange)="applyFilters()" />
      </label>
      <label class="field">
        <span>Hasta</span>
        <input class="control" type="date" [(ngModel)]="toDate" (ngModelChange)="applyFilters()" />
      </label>
      <button class="secondary-button finance-reset" type="button" (click)="resetDates()">Limpiar rango</button>
    </section>

    <section class="dashboard-grid metrics-grid finance-metrics">
      <p-card styleClass="metric-card"><span class="metric-label">Reparaciones</span><div class="metric">{{ filteredRepairs.length }}</div><small>Incluidas en el rango</small></p-card>
      <p-card styleClass="metric-card"><span class="metric-label">Ingresos</span><div class="metric">{{ totalIncome | currency:'ARS':'symbol':'1.0-0':'es-AR' }}</div><small>Total facturado</small></p-card>
      <p-card styleClass="metric-card"><span class="metric-label">Gasto en repuestos</span><div class="metric">{{ totalPartsCost | currency:'ARS':'symbol':'1.0-0':'es-AR' }}</div><small>Suma de costos</small></p-card>
      <p-card styleClass="metric-card revenue"><span class="metric-label">Ganancia neta</span><div class="metric">{{ netIncome | currency:'ARS':'symbol':'1.0-0':'es-AR' }}</div><small>Ingresos menos repuestos</small></p-card>
    </section>

    <section class="dashboard-grid lists finance-layout">
      <p-card header="Composición del periodo">
        <div class="ops-summary-grid finance-breakdown">
          <div class="ops-item"><span>Mano de obra cargada</span><strong>{{ totalLabor | currency:'ARS':'symbol':'1.0-0':'es-AR' }}</strong></div>
          <div class="ops-item"><span>Presupuestos emitidos</span><strong>{{ totalQuoted | currency:'ARS':'symbol':'1.0-0':'es-AR' }}</strong></div>
          <div class="ops-item"><span>Ordenes entregadas</span><strong>{{ deliveredCount }}</strong></div>
          <div class="ops-item"><span>Margen promedio</span><strong>{{ averageNet | currency:'ARS':'symbol':'1.0-0':'es-AR' }}</strong></div>
        </div>
      </p-card>

      <p-card header="Detalle por reparación">
        <div class="native-table-wrap">
          <table class="native-table finance-table">
            <thead><tr><th>Orden</th><th>Fecha</th><th>Estado</th><th>Ingreso</th><th>Repuestos</th><th>Neto</th></tr></thead>
            <tbody>
              @for (row of financeRows; track row.orderNumber + row.dateLabel) {
                <tr>
                  <td>#{{ row.orderNumber || '-' }}</td>
                  <td>{{ row.dateLabel }}</td>
                  <td>{{ row.statusLabel }}</td>
                  <td>{{ row.income | currency:'ARS':'symbol':'1.0-0':'es-AR' }}</td>
                  <td>{{ row.partsCost | currency:'ARS':'symbol':'1.0-0':'es-AR' }}</td>
                  <td>{{ row.net | currency:'ARS':'symbol':'1.0-0':'es-AR' }}</td>
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
  repairs: Repair[] = [];
  filteredRepairs: Repair[] = [];
  financeRows: FinanceRow[] = [];
  fromDate = '';
  toDate = '';
  totalIncome = 0;
  totalPartsCost = 0;
  totalLabor = 0;
  totalQuoted = 0;
  netIncome = 0;
  averageNet = 0;
  deliveredCount = 0;

  constructor(private readonly api: ApiService, private readonly changeDetector: ChangeDetectorRef) {}

  ngOnInit(): void {
    forkJoin({ repairs: this.api.getRepairs() }).subscribe(({ repairs }) => {
      this.repairs = repairs.slice().reverse();
      this.applyFilters();
      this.changeDetector.detectChanges();
    });
  }

  applyFilters(): void {
    const from = this.fromDate ? new Date(`${this.fromDate}T00:00:00`) : null;
    const to = this.toDate ? new Date(`${this.toDate}T23:59:59`) : null;

    this.filteredRepairs = this.repairs.filter((repair) => {
      const movement = this.resolveMovementDate(repair);
      if (!movement) return !from && !to;
      const movementDate = new Date(movement);
      const matchesFrom = !from || movementDate >= from;
      const matchesTo = !to || movementDate <= to;
      return matchesFrom && matchesTo;
    });

    this.financeRows = this.filteredRepairs.map((repair) => {
      const income = this.asMoney(repair.price);
      const partsCost = this.sumPartsCost(repair.parts || []);
      return {
        orderNumber: repair.orderNumber,
        dateLabel: this.resolveMovementDate(repair) ? new Date(this.resolveMovementDate(repair)!).toLocaleDateString('es-AR') : '-',
        statusLabel: this.statusLabel(repair.status),
        income,
        partsCost,
        net: income - partsCost
      };
    });

    this.totalIncome = this.financeRows.reduce((acc, row) => acc + row.income, 0);
    this.totalPartsCost = this.financeRows.reduce((acc, row) => acc + row.partsCost, 0);
    this.totalLabor = this.filteredRepairs.reduce((acc, repair) => acc + this.asMoney(repair.laborAmount), 0);
    this.totalQuoted = this.filteredRepairs.reduce((acc, repair) => acc + this.asMoney(repair.quotedAmount), 0);
    this.netIncome = this.totalIncome - this.totalPartsCost;
    this.averageNet = this.financeRows.length ? this.netIncome / this.financeRows.length : 0;
    this.deliveredCount = this.filteredRepairs.filter((repair) => repair.status === 'RETIRADA').length;
  }

  resetDates(): void {
    this.fromDate = '';
    this.toDate = '';
    this.applyFilters();
  }

  private resolveMovementDate(repair: Repair): string | undefined {
    return repair.returnDateTime || repair.receiveDateTime;
  }

  private sumPartsCost(parts: RepairPart[]): number {
    return parts.reduce((acc, part) => acc + (this.asMoney(part.cost) * this.asMoney(part.quantity || 1)), 0);
  }

  private asMoney(value: unknown): number {
    const parsed = Number(value ?? 0);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  private statusLabel(status: Repair['status']): string {
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
}
