import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
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
      <p>Lectura rápida de carga de trabajo y últimos movimientos del taller.</p>
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
    </section>

    <section class="dashboard-grid charts" *ngIf="dataReady">
      <p-card header="Equipos por tipo">
        <div class="chart-surface">
          <p-chart *ngIf="chartsVisible" type="doughnut" [data]="deviceTypeChartData" [options]="doughnutChartOptions"></p-chart>
        </div>
      </p-card>
      <p-card header="Reparaciones por estado">
        <div class="chart-surface">
          <p-chart *ngIf="chartsVisible" type="doughnut" [data]="repairStatusChartData" [options]="doughnutChartOptions"></p-chart>
        </div>
      </p-card>
    </section>

    <section class="dashboard-grid lists" *ngIf="dataReady">
      <p-card header="Últimos 5 clientes">
        <div class="native-table-wrap">
          <table class="native-table dashboard-table">
            <thead><tr><th>Nombre</th><th>Tipo de dispositivo</th></tr></thead>
            <tbody>
              <tr *ngFor="let item of overview.recentClients; trackBy: recentClientTrack">
                <td>{{ item.name }}</td>
                <td>{{ item.deviceType }}</td>
              </tr>
              <tr *ngIf="!overview.recentClients.length"><td class="empty-cell" colspan="2">Sin datos recientes.</td></tr>
            </tbody>
          </table>
        </div>
      </p-card>

      <p-card header="Últimos 5 dispositivos">
        <div class="native-table-wrap">
          <table class="native-table dashboard-table">
            <thead><tr><th>Tipo</th><th>Marca</th><th>Modelo</th></tr></thead>
            <tbody>
              <tr *ngFor="let item of overview.recentDevices; trackBy: recentDeviceTrack">
                <td>{{ item.deviceType }}</td>
                <td>{{ item.brand }}</td>
                <td>{{ item.model }}</td>
              </tr>
              <tr *ngIf="!overview.recentDevices.length"><td class="empty-cell" colspan="3">Sin dispositivos recientes.</td></tr>
            </tbody>
          </table>
        </div>
      </p-card>

      <p-card header="Últimas 5 reparaciones">
        <div class="native-table-wrap">
          <table class="native-table dashboard-table">
            <thead><tr><th>Fecha</th><th>Cliente</th><th>Monto</th></tr></thead>
            <tbody>
              <tr *ngFor="let item of overview.recentRepairs; trackBy: recentRepairTrack">
                <td>{{ formatDate(item.date) }}</td>
                <td>{{ item.client }}</td>
                <td>{{ item.price != null ? (asMoney(item.price) | currency:'ARS':'symbol':'1.2-2':'es-AR') : '-' }}</td>
              </tr>
              <tr *ngIf="!overview.recentRepairs.length"><td class="empty-cell" colspan="3">Sin reparaciones entregadas.</td></tr>
            </tbody>
          </table>
        </div>
      </p-card>

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
    deviceTypes: [],
    repairStatuses: [],
    recentClients: [],
    recentDevices: [],
    recentRepairs: [],
    inactiveDevices: []
  };
  dataReady = false;
  chartsVisible = false;
  themeMode: ThemeMode;
  deviceTypeChartData: any = { labels: [], datasets: [] };
  repairStatusChartData: any = { labels: [], datasets: [] };
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
        if (this.dataReady) {
          this.refreshCharts(true);
        }
      })
    );

    this.subscriptions.add(
      this.api.getDashboardOverview().subscribe((overview) => {
        this.overview = {
          ...overview,
          recentClients: overview.recentClients || [],
          recentDevices: overview.recentDevices || [],
          recentRepairs: overview.recentRepairs || [],
          inactiveDevices: overview.inactiveDevices || [],
          deviceTypes: overview.deviceTypes || [],
          repairStatuses: overview.repairStatuses || []
        };
        this.dataReady = true;
        this.refreshCharts(true);
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  recentClientTrack = (_: number, item: DashboardOverview['recentClients'][number]) => item.id;
  recentDeviceTrack = (_: number, item: DashboardOverview['recentDevices'][number]) => item.id;
  recentRepairTrack = (_: number, item: DashboardOverview['recentRepairs'][number]) => item.repairId;
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
    const mutedColor = this.themeMode === 'dark' ? '#a6b0b4' : '#667176';
    const borderColor = this.themeMode === 'dark' ? '#303940' : '#d7d2c8';
    const brandColor = '#0c8a9f';
    const accentColor = '#d79324';
    const successColor = '#177245';
    const dangerColor = '#b73636';
    const infoColor = '#2364aa';
    const palette = [brandColor, accentColor, successColor, infoColor, '#8b5cf6', '#ef4444', '#14b8a6', '#f97316'];

    this.deviceTypeChartData = {
      labels: this.overview.deviceTypes.map((row) => row.label),
      datasets: [{
        data: this.overview.deviceTypes.map((row) => this.asMoney(row.value)),
        backgroundColor: palette.slice(0, Math.max(this.overview.deviceTypes.length, 1)),
        borderColor: this.themeMode === 'dark' ? '#171b1f' : '#ffffff',
        borderWidth: 2
      }]
    };

    this.repairStatusChartData = {
      labels: this.overview.repairStatuses.map((row) => row.label),
      datasets: [{
        data: this.overview.repairStatuses.map((row) => this.asMoney(row.value)),
        backgroundColor: [infoColor, brandColor, accentColor, successColor, dangerColor, '#6b7280'],
        borderColor: this.themeMode === 'dark' ? '#171b1f' : '#ffffff',
        borderWidth: 2
      }]
    };

    this.doughnutChartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '62%',
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
        this.chartsVisible = true;
        this.changeDetector.detectChanges();
      });
    });
  }
}
