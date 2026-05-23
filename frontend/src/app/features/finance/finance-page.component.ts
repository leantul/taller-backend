import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, NgZone, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { CardModule } from 'primeng/card';
import { UIChart } from 'primeng/chart';
import { MessageService } from 'primeng/api';
import { ApiService } from '../../core/services/api.service';
import { FinanceRow, FinanceSummary } from '../../shared/models/finance.model';
import { Repair } from '../../shared/models/repair.model';
import { ThemeMode, ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'app-finance-page',
  standalone: true,
  imports: [CommonModule, FormsModule, CardModule, UIChart],
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

    <section class="dashboard-grid charts finance-charts">
      <p-card header="Ganancia neta por mes">
        <div class="chart-surface">
          <p-chart *ngIf="chartVisible" type="bar" [data]="monthlyNetChartData" [options]="barChartOptions"></p-chart>
        </div>
      </p-card>
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
            <thead><tr><th>Orden</th><th>Fecha</th><th>Ingreso</th><th>Repuestos</th><th>Neto</th></tr></thead>
            <tbody>
              @for (row of deliveredFinanceRows; track row.repairId + row.date) {
                <tr>
                  <td>#{{ row.orderNumber || '-' }}</td>
                  <td>{{ row.date ? (row.date | date:'dd/MM/yyyy') : '-' }}</td>
                  <td>{{ formatMoney(row.income) }}</td>
                  <td>{{ formatMoney(row.partsCost) }}</td>
                  <td>{{ formatMoney(row.net) }}</td>
                </tr>
              } @empty {
                <tr><td class="empty-cell" colspan="5">No hay reparaciones retiradas dentro del rango elegido.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </p-card>
    </section>
  `
})
export class FinancePageComponent implements OnInit, OnDestroy {
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
  themeMode: ThemeMode;
  chartVisible = false;
  monthlyNetChartData: any = { labels: [], datasets: [] };
  barChartOptions: any = {};
  private readonly subscriptions = new Subscription();

  constructor(
    private readonly api: ApiService,
    private readonly changeDetector: ChangeDetectorRef,
    private readonly themeService: ThemeService,
    private readonly messageService: MessageService,
    private readonly zone: NgZone
  ) {
    this.themeMode = this.themeService.currentTheme();
  }

  ngOnInit(): void {
    this.subscriptions.add(
      this.themeService.mode$.subscribe((mode) => {
        this.zone.run(() => {
          this.themeMode = mode;
          if (this.lastSummary) {
            this.buildMonthlyNetChart(this.lastSummary);
          }
        });
      })
    );
    this.setCurrentMonthRange();
    this.applyFilters();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  private lastSummary: FinanceSummary | null = null;

  applyFilters(): void {
    this.api.getFinanceSummary(this.draftFromDate || undefined, this.draftToDate || undefined).subscribe({
      next: (summary) => {
        this.zone.run(() => {
          this.lastSummary = summary;
          this.hydrateSummary(summary);
          this.buildMonthlyNetChart(summary);
          this.changeDetector.detectChanges();
        });
      },
      error: (error) => {
        this.zone.run(() => {
          this.chartVisible = false;
          this.messageService.add({
            severity: 'error',
            summary: 'No se pudo cargar Finanzas',
            detail: this.financeErrorDetail(error)
          });
          this.changeDetector.detectChanges();
        });
      }
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

  get deliveredFinanceRows(): FinanceRow[] {
    return this.financeRows.filter((row) => row.status === 'RETIRADA');
  }

  private buildMonthlyNetChart(summary: FinanceSummary): void {
    const styles = getComputedStyle(document.documentElement);
    const textColor = styles.getPropertyValue('--text').trim() || '#1d2529';
    const borderColor = styles.getPropertyValue('--border').trim() || '#d7d2c8';
    const brandColor = styles.getPropertyValue('--brand-500').trim() || '#0c8a9f';

    this.monthlyNetChartData = {
      labels: (summary.monthlyNet || []).map((row) => row.label),
      datasets: [{
        label: 'Ganancia neta',
        data: (summary.monthlyNet || []).map((row) => this.asMoney(row.value)),
        backgroundColor: colorWithAlpha(brandColor, 0.72),
        borderColor: brandColor,
        borderWidth: 1.5,
        borderRadius: 8,
        maxBarThickness: 34
      }]
    };

    this.barChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        },
        tooltip: {
          backgroundColor: this.themeMode === 'dark' ? '#111417' : '#ffffff',
          titleColor: textColor,
          bodyColor: textColor,
          borderColor,
          borderWidth: 1
        }
      },
      scales: {
        x: {
          ticks: { color: textColor },
          grid: { color: colorWithAlpha(borderColor, 0.35) }
        },
        y: {
          ticks: { color: textColor },
          grid: { color: colorWithAlpha(borderColor, 0.35) }
        }
      }
    };

    this.chartVisible = false;
    this.changeDetector.detectChanges();
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        this.zone.run(() => {
          this.chartVisible = true;
          this.changeDetector.detectChanges();
        });
      });
    });
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

  private financeErrorDetail(error: any): string {
    if (error?.status === 403) {
      return 'No autorizado (403). La sesion se mantiene y la pantalla queda abierta; revisa permisos o el endpoint.';
    }

    if (error?.status === 401) {
      return 'Sesion vencida o token invalido. Volve a iniciar sesion.';
    }

    return `Error ${error?.status || 'sin codigo'} al consultar el resumen.`;
  }
}

function colorWithAlpha(hex: string, alpha: number): string {
  if (!hex.startsWith('#')) {
    return hex;
  }
  const normalized = hex.length === 4
    ? `#${hex[1]}${hex[1]}${hex[2]}${hex[2]}${hex[3]}${hex[3]}`
    : hex;
  const r = Number.parseInt(normalized.slice(1, 3), 16);
  const g = Number.parseInt(normalized.slice(3, 5), 16);
  const b = Number.parseInt(normalized.slice(5, 7), 16);
  return `rgba(${r}, ${g}, ${b}, ${alpha})`;
}
