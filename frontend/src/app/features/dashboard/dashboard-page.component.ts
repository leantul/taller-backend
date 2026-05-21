import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { CardModule } from 'primeng/card';
import { UIChart } from 'primeng/chart';
import { ApiService } from '../../core/services/api.service';
import { ThemeMode, ThemeService } from '../../core/services/theme.service';
import { DashboardInactiveDevice, DashboardOverview, DashboardRecentClient, DashboardRecentDevice, DashboardRecentRepair, DashboardSeriesItem } from '../../shared/models/dashboard.model';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, RouterLink, CardModule, UIChart],
  template: `
    <section class="page-heading">
      <div>
        <span class="eyebrow">Taller activo</span>
        <h1>Resumen operativo</h1>
      </div>
      <p>Lectura rápida de carga de trabajo, ingresos y últimos movimientos.</p>
    </section>

    <section class="dashboard-topline">
      <div class="action-hub">
        <a routerLink="/reparaciones" class="action-tile">
          <i class="pi pi-wrench"></i>
          <div>
            <strong>Nueva reparación</strong>
            <small>Alta, presupuesto y seguimiento</small>
          </div>
        </a>
        <a routerLink="/clientes" class="action-tile">
          <i class="pi pi-users"></i>
          <div>
            <strong>Nuevo cliente</strong>
            <small>Base de clientes y datos de contacto</small>
          </div>
        </a>
        <a routerLink="/dispositivos" class="action-tile">
          <i class="pi pi-desktop"></i>
          <div>
            <strong>Nuevo dispositivo</strong>
            <small>Vincular equipo con su dueño</small>
          </div>
        </a>
        <a routerLink="/status" class="action-tile">
          <i class="pi pi-th-large"></i>
          <div>
            <strong>Ver tablero</strong>
            <small>Mover ordenes entre etapas</small>
          </div>
        </a>
      </div>

      <div class="ops-summary">
        <div class="ops-summary-head">
          <strong>Foco operativo</strong>
          <small>Lo que conviene mirar primero</small>
        </div>
        <div class="ops-summary-grid">
          <div class="ops-item"><span>Pendientes de retiro</span><strong>{{ overview.waitingPickupCount }}</strong></div>
          <div class="ops-item"><span>En proceso</span><strong>{{ overview.inProgressCount }}</strong></div>
          <div class="ops-item"><span>Presupuestadas</span><strong>{{ overview.quotedPendingCount }}</strong></div>
          <div class="ops-item"><span>Ingreso este mes</span><strong>{{ asMoney(overview.monthRevenue) | currency:'ARS':'symbol':'1.0-0':'es-AR' }}</strong></div>
        </div>
      </div>
    </section>

    <section class="dashboard-grid metrics-grid">
      <p-card styleClass="metric-card"><span class="metric-label">Clientes</span><div class="metric">{{ overview.clientCount }}</div><small>Total registrados</small></p-card>
      <p-card styleClass="metric-card"><span class="metric-label">Dispositivos</span><div class="metric">{{ overview.deviceCount }}</div><small>Equipos cargados</small></p-card>
      <p-card styleClass="metric-card"><span class="metric-label">Reparaciones</span><div class="metric">{{ overview.repairCount }}</div><small>Ordenes históricas</small></p-card>
      <p-card styleClass="metric-card revenue"><span class="metric-label">Ingresos estimados</span><div class="metric">{{ asMoney(overview.totalRevenue) | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</div><small>Suma de reparaciones</small></p-card>
    </section>

    <section class="dashboard-grid charts">
      <p-card header="Ingresos por mes">
        <div class="chart-surface">
          @if (chartsReady) {
            <p-chart type="bar" [data]="monthlyRevenueChartData" [options]="barChartOptions"></p-chart>
          } @else {
            <div class="chart-placeholder">Preparando gráfico…</div>
          }
        </div>
      </p-card>
      <p-card header="Equipos por tipo">
        <div class="chart-surface">
          @if (chartsReady) {
            <p-chart type="doughnut" [data]="deviceTypeChartData" [options]="doughnutChartOptions"></p-chart>
          } @else {
            <div class="chart-placeholder">Preparando gráfico…</div>
          }
        </div>
      </p-card>
      <p-card header="Reparaciones por estado">
        <div class="chart-surface">
          @if (chartsReady) {
            <p-chart type="doughnut" [data]="repairStatusChartData" [options]="doughnutChartOptions"></p-chart>
          } @else {
            <div class="chart-placeholder">Preparando gráfico…</div>
          }
        </div>
      </p-card>
    </section>

    <section class="dashboard-grid lists">
      <p-card header="Últimos 5 clientes">
        <div class="native-table-wrap">
          <table class="native-table dashboard-table">
            <thead><tr><th>Nombre</th><th>Tipo de dispositivo</th></tr></thead>
            <tbody>
              @for (item of overview.recentClients; track item.id) {
                <tr><td>{{ item.name }}</td><td>{{ item.deviceType }}</td></tr>
              } @empty {
                <tr><td class="empty-cell" colspan="2">Sin datos recientes.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </p-card>

      <p-card header="Últimos 5 dispositivos">
        <div class="native-table-wrap">
          <table class="native-table dashboard-table">
            <thead><tr><th>Tipo</th><th>Marca</th><th>Modelo</th></tr></thead>
            <tbody>
              @for (item of overview.recentDevices; track item.id) {
                <tr><td>{{ item.deviceType }}</td><td>{{ item.brand }}</td><td>{{ item.model }}</td></tr>
              } @empty {
                <tr><td class="empty-cell" colspan="3">Sin dispositivos recientes.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </p-card>

      <p-card header="Últimas 5 reparaciones">
        <div class="native-table-wrap">
          <table class="native-table dashboard-table">
            <thead><tr><th>Fecha</th><th>Cliente</th><th>Monto</th></tr></thead>
            <tbody>
              @for (item of overview.recentRepairs; track item.repairId) {
                <tr><td>{{ formatDate(item.date) }}</td><td>{{ item.client }}</td><td>{{ asMoney(item.price) | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</td></tr>
              } @empty {
                <tr><td class="empty-cell" colspan="3">Sin reparaciones entregadas.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </p-card>

      <p-card header="Top 5 equipos inactivos">
        <div class="native-table-wrap">
          <table class="native-table dashboard-table">
            <thead><tr><th>Cliente</th><th>Fecha</th></tr></thead>
            <tbody>
              @for (item of overview.inactiveDevices; track item.name) {
                <tr><td>{{ item.name }}</td><td>{{ item.lastRepair ? formatDate(item.lastRepair) : 'Sin historial' }}</td></tr>
              } @empty {
                <tr><td class="empty-cell" colspan="2">Sin registros para mostrar.</td></tr>
              }
            </tbody>
          </table>
        </div>
      </p-card>
    </section>
  `
})
export class DashboardPageComponent implements OnInit, OnDestroy {
  overview: DashboardOverview = {
    clientCount: 0,
    deviceCount: 0,
    repairCount: 0,
    totalRevenue: 0,
    monthRevenue: 0,
    waitingPickupCount: 0,
    inProgressCount: 0,
    quotedPendingCount: 0,
    monthlyRevenue: [],
    deviceTypes: [],
    repairStatuses: [],
    recentClients: [],
    recentDevices: [],
    recentRepairs: [],
    inactiveDevices: []
  };
  chartsReady = false;
  themeMode: ThemeMode;
  monthlyRevenueChartData: any = { labels: [], datasets: [] };
  deviceTypeChartData: any = { labels: [], datasets: [] };
  repairStatusChartData: any = { labels: [], datasets: [] };
  barChartOptions: any = {};
  doughnutChartOptions: any = {};

  private readonly subscriptions = new Subscription();

  constructor(
    private readonly api: ApiService,
    private readonly changeDetector: ChangeDetectorRef,
    private readonly themeService: ThemeService
  ) {
    this.themeMode = this.themeService.currentTheme();
  }

  ngOnInit(): void {
    this.subscriptions.add(
      this.themeService.mode$.subscribe((mode) => {
        this.themeMode = mode;
        if (this.overview.monthlyRevenue.length || this.overview.deviceTypes.length || this.overview.repairStatuses.length) {
          this.refreshCharts();
        }
      })
    );

    this.subscriptions.add(
      this.api.getDashboardOverview().subscribe((overview) => {
        this.overview = overview;
        this.refreshCharts();
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  asMoney(value: unknown): number {
    const parsed = Number(value ?? 0);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  formatDate(value: string | null | undefined): string {
    if (!value) {
      return '-';
    }
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleDateString('es-AR');
  }

  private refreshCharts(): void {
    const styles = getComputedStyle(document.documentElement);
    const textColor = styles.getPropertyValue('--text').trim() || '#1d2529';
    const mutedColor = styles.getPropertyValue('--muted').trim() || '#667176';
    const borderColor = styles.getPropertyValue('--border').trim() || '#d7d2c8';
    const brandColor = styles.getPropertyValue('--brand-500').trim() || '#0c8a9f';
    const accentColor = styles.getPropertyValue('--accent-500').trim() || '#d79324';
    const successColor = styles.getPropertyValue('--success-500').trim() || '#177245';
    const dangerColor = styles.getPropertyValue('--danger-500').trim() || '#b73636';
    const infoColor = styles.getPropertyValue('--info-500').trim() || '#2364aa';

    const palette = [brandColor, accentColor, successColor, infoColor, '#8b5cf6', '#ef4444', '#14b8a6', '#f97316'];

    this.monthlyRevenueChartData = {
      labels: this.overview.monthlyRevenue.map((row) => row.label),
      datasets: [
        {
          label: 'Ingresos',
          data: this.overview.monthlyRevenue.map((row) => this.asMoney(row.value)),
          backgroundColor: colorWithAlpha(brandColor, 0.75),
          borderColor: brandColor,
          borderWidth: 1.5,
          borderRadius: 8,
          maxBarThickness: 36
        }
      ]
    };

    this.deviceTypeChartData = {
      labels: this.overview.deviceTypes.map((row) => row.label),
      datasets: [
        {
          data: this.overview.deviceTypes.map((row) => this.asMoney(row.value)),
          backgroundColor: palette.slice(0, Math.max(this.overview.deviceTypes.length, 1)),
          borderColor: this.themeMode === 'dark' ? '#171b1f' : '#ffffff',
          borderWidth: 2
        }
      ]
    };

    this.repairStatusChartData = {
      labels: this.overview.repairStatuses.map((row) => row.label),
      datasets: [
        {
          data: this.overview.repairStatuses.map((row) => this.asMoney(row.value)),
          backgroundColor: [infoColor, brandColor, accentColor, successColor, dangerColor, '#6b7280'],
          borderColor: this.themeMode === 'dark' ? '#171b1f' : '#ffffff',
          borderWidth: 2
        }
      ]
    };

    this.barChartOptions = {
      maintainAspectRatio: false,
      animation: false,
      plugins: {
        legend: {
          display: false,
          labels: { color: textColor }
        },
        tooltip: {
          titleColor: textColor,
          bodyColor: textColor
        }
      },
      scales: {
        x: {
          ticks: { color: mutedColor },
          grid: { display: false, color: borderColor }
        },
        y: {
          beginAtZero: true,
          ticks: { color: mutedColor },
          grid: { color: colorWithAlpha(borderColor, 0.75) }
        }
      }
    };

    this.doughnutChartOptions = {
      maintainAspectRatio: false,
      animation: false,
      cutout: '62%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            color: textColor,
            usePointStyle: true,
            boxWidth: 10,
            padding: 16
          }
        },
        tooltip: {
          titleColor: textColor,
          bodyColor: textColor
        }
      }
    };

    this.chartsReady = true;
    this.changeDetector.detectChanges();
  }
}

function colorWithAlpha(hex: string, alpha: number): string {
  const clean = hex.replace('#', '');
  if (clean.length !== 6) {
    return hex;
  }
  const red = parseInt(clean.slice(0, 2), 16);
  const green = parseInt(clean.slice(2, 4), 16);
  const blue = parseInt(clean.slice(4, 6), 16);
  return `rgba(${red}, ${green}, ${blue}, ${alpha})`;
}
