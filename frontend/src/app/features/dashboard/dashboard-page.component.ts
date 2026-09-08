import { ChangeDetectorRef, Component, NgZone, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { CardModule } from 'primeng/card';
import { UIChart } from 'primeng/chart';
import { ApiService } from '../../core/services/api.service';
import { ThemeMode, ThemeService } from '../../core/services/theme.service';
import { DashboardOverview } from '../../shared/models/dashboard.model';

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
      <p>Lectura rápida de carga de trabajo, evolución y demoras del taller.</p>
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
        <div class="ops-summary-grid dashboard-focus-grid">
          <div class="ops-item"><span>Pendientes de retiro</span><strong>{{ overview.waitingPickupCount }}</strong></div>
          <div class="ops-item"><span>En proceso</span><strong>{{ overview.inProgressCount }}</strong></div>
          <div class="ops-item"><span>Presupuestadas</span><strong>{{ overview.quotedPendingCount }}</strong></div>
        </div>
      </div>
    </section>

    <section class="dashboard-grid dashboard-metrics">
      <p-card styleClass="metric-card"><span class="metric-label">Clientes</span><div class="metric">{{ overview.clientCount }}</div><small>Total registrados</small></p-card>
      <p-card styleClass="metric-card"><span class="metric-label">Dispositivos</span><div class="metric">{{ overview.deviceCount }}</div><small>Equipos cargados</small></p-card>
      <p-card styleClass="metric-card"><span class="metric-label">Reparaciones</span><div class="metric">{{ overview.repairCount }}</div><small>Ordenes históricas</small></p-card>
      <p-card styleClass="metric-card"><span class="metric-label">Tiempo de ciclo</span><div class="metric">{{ overview.averageTurnaroundDays | number:'1.1-1':'es-AR' }} días</div><small>Promedio de recepción a retiro</small></p-card>
      <p-card styleClass="metric-card"><span class="metric-label">Órdenes demoradas</span><div class="metric metric-warning">{{ overview.overdueOpenRepairs }}</div><small>Abiertas hace más de 7 días</small></p-card>
    </section>

    <section class="dashboard-grid charts" *ngIf="dataReady">
      <p-card header="Reparaciones recibidas por mes" styleClass="dashboard-wide-card">
        <div class="chart-surface">
          <p-chart *ngIf="chartsVisible" type="line" [data]="monthlyReceivedChartData" [options]="monthlyReceivedChartOptions"></p-chart>
        </div>
      </p-card>
    </section>

    <section class="dashboard-grid lists dashboard-inactive-list" *ngIf="dataReady">
      <p-card header="Top 5 equipos inactivos">
        <div class="native-table-wrap">
          <table class="native-table dashboard-table">
            <thead><tr><th>Cliente</th><th>Fecha</th></tr></thead>
            <tbody>
              <tr *ngFor="let item of overview.inactiveDevices; trackBy: inactiveDeviceTrack">
                <td>{{ item.name }}</td>
                <td>{{ item.lastRepair ? formatDate(item.lastRepair) : 'Sin historial' }}</td>
              </tr>
              <tr *ngIf="!overview.inactiveDevices.length"><td class="empty-cell" colspan="2">Sin registros para mostrar.</td></tr>
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
    waitingPickupCount: 0,
    inProgressCount: 0,
    quotedPendingCount: 0,
    monthlyReceivedRepairs: [],
    averageTurnaroundDays: 0,
    overdueOpenRepairs: 0,
    inactiveDevices: []
  };
  dataReady = false;
  chartsVisible = false;
  themeMode: ThemeMode;
  monthlyReceivedChartData: any = { labels: [], datasets: [] };
  monthlyReceivedChartOptions: any = {};

  private readonly subscriptions = new Subscription();

  constructor(
    private readonly api: ApiService,
    private readonly changeDetector: ChangeDetectorRef,
    private readonly themeService: ThemeService,
    private readonly zone: NgZone
  ) {
    this.themeMode = this.themeService.currentTheme();
  }

  ngOnInit(): void {
    this.subscriptions.add(
      this.themeService.mode$.subscribe((mode) => {
        this.zone.run(() => {
          this.themeMode = mode;
          if (this.dataReady) {
            this.refreshCharts(true);
          }
        });
      })
    );

    this.subscriptions.add(
      this.api.getDashboardOverview().subscribe((overview) => {
        this.zone.run(() => {
          this.overview = {
            ...overview,
            inactiveDevices: overview.inactiveDevices || [],
            monthlyReceivedRepairs: overview.monthlyReceivedRepairs || [],
            averageTurnaroundDays: this.asMoney(overview.averageTurnaroundDays),
            overdueOpenRepairs: this.asMoney(overview.overdueOpenRepairs)
          };
          this.dataReady = true;
          this.changeDetector.detectChanges();
          this.refreshCharts(true);
        });
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  inactiveDeviceTrack = (_: number, item: DashboardOverview['inactiveDevices'][number]) => item.name;

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

  private refreshCharts(remount: boolean): void {
    const textColor = this.themeMode === 'dark' ? '#eef2f3' : '#1d2529';
    const borderColor = this.themeMode === 'dark' ? '#303940' : '#d7d2c8';
    const brandColor = '#0c8a9f';
    this.monthlyReceivedChartData = {
      labels: this.overview.monthlyReceivedRepairs.map((row) => row.label),
      datasets: [{
        label: 'Reparaciones recibidas',
        data: this.overview.monthlyReceivedRepairs.map((row) => this.asMoney(row.value)),
        borderColor: brandColor,
        backgroundColor: this.themeMode === 'dark' ? 'rgba(32, 190, 212, .18)' : 'rgba(12, 138, 159, .16)',
        borderWidth: 3,
        pointBackgroundColor: brandColor,
        pointBorderColor: this.themeMode === 'dark' ? '#171b1f' : '#ffffff',
        pointBorderWidth: 2,
        pointRadius: 4,
        tension: .35,
        fill: true
      }]
    };

    this.monthlyReceivedChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: { ticks: { color: textColor }, grid: { display: false } },
        y: { beginAtZero: true, ticks: { color: textColor, precision: 0 }, grid: { color: borderColor } }
      },
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            color: textColor,
            usePointStyle: true,
            padding: 16,
            boxWidth: 10,
            boxHeight: 10
          }
        },
        tooltip: {
          backgroundColor: this.themeMode === 'dark' ? '#111417' : '#ffffff',
          titleColor: textColor,
          bodyColor: textColor,
          borderColor: borderColor,
          borderWidth: 1
        }
      }
    };

    if (!remount) {
      this.changeDetector.detectChanges();
      return;
    }

    this.chartsVisible = false;
    this.changeDetector.detectChanges();
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        this.zone.run(() => {
          this.chartsVisible = true;
          this.changeDetector.detectChanges();
        });
      });
    });
  }
}
