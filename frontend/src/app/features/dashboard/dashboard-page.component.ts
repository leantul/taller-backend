import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin, Subscription } from 'rxjs';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';
import { ApiService } from '../../core/services/api.service';
import { ThemeMode, ThemeService } from '../../core/services/theme.service';
import { Client } from '../../shared/models/client.model';
import { Device } from '../../shared/models/device.model';
import { Repair } from '../../shared/models/repair.model';

type BreakdownRow = { label: string; value: number };

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, RouterLink, CardModule, ChartModule],
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
          <div class="ops-item"><span>Pendientes de retiro</span><strong>{{ waitingPickupCount }}</strong></div>
          <div class="ops-item"><span>En proceso</span><strong>{{ inProgressCount }}</strong></div>
          <div class="ops-item"><span>Presupuestadas</span><strong>{{ quotedPendingCount }}</strong></div>
          <div class="ops-item"><span>Ingreso este mes</span><strong>{{ monthRevenue | currency:'ARS':'symbol':'1.0-0':'es-AR' }}</strong></div>
        </div>
      </div>
    </section>

    <section class="dashboard-grid metrics-grid">
      <p-card styleClass="metric-card"><span class="metric-label">Clientes</span><div class="metric">{{ clients.length }}</div><small>Total registrados</small></p-card>
      <p-card styleClass="metric-card"><span class="metric-label">Dispositivos</span><div class="metric">{{ devices.length }}</div><small>Equipos cargados</small></p-card>
      <p-card styleClass="metric-card"><span class="metric-label">Reparaciones</span><div class="metric">{{ repairs.length }}</div><small>Ordenes históricas</small></p-card>
      <p-card styleClass="metric-card revenue"><span class="metric-label">Ingresos estimados</span><div class="metric">{{ totalRevenue | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</div><small>Suma de reparaciones</small></p-card>
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
              @for (item of recentClients; track item.name) {
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
              @for (item of recentDevices; track item.id || item.serialNumber) {
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
              @for (item of recentRepairs; track item.date + item.client) {
                <tr><td>{{ item.date }}</td><td>{{ item.client }}</td><td>{{ item.price | currency:'ARS':'symbol':'1.2-2':'es-AR' }}</td></tr>
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
              @for (item of inactiveClients; track item.name) {
                <tr><td>{{ item.name }}</td><td>{{ item.lastRepair || 'Sin historial' }}</td></tr>
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
  clients: Client[] = [];
  devices: Device[] = [];
  repairs: Repair[] = [];
  totalRevenue = 0;
  monthRevenue = 0;
  recentClients: { name: string; deviceType: string }[] = [];
  recentDevices: Device[] = [];
  recentRepairs: { date: string; client: string; price: number }[] = [];
  inactiveClients: { name: string; lastRepair: string | null }[] = [];
  deviceTypeRows: BreakdownRow[] = [];
  repairStatusRows: BreakdownRow[] = [];
  monthlyRevenueRows: BreakdownRow[] = [];
  waitingPickupCount = 0;
  inProgressCount = 0;
  quotedPendingCount = 0;
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
        if (this.repairs.length || this.devices.length) {
          this.refreshCharts();
        }
      })
    );

    this.subscriptions.add(
      forkJoin({
        clients: this.api.getClients(),
        devices: this.api.getDevices(),
        repairs: this.api.getRepairs(),
        latestClients: this.api.getLatestClients(),
        latestDevices: this.api.getLatestDevices(),
        latestRepairs: this.api.getLatestRepairs()
      }).subscribe(({ clients, devices, repairs, latestClients, latestDevices }) => {
        this.clients = clients;
        this.devices = devices;
        this.repairs = repairs;
        this.totalRevenue = repairs.reduce((acc, item) => acc + this.asMoney(item.price), 0);
        this.waitingPickupCount = repairs.filter((item) => item.status === 'ESPERANDO_RETIRO').length;
        this.inProgressCount = repairs.filter((item) => item.status === 'HACIENDO' || item.status === 'RECIBIDA').length;
        this.quotedPendingCount = repairs.filter((item) => item.status === 'PRESUPUESTADA_ESPERANDO_RESPUESTA').length;

        this.recentClients = latestClients.map((client) => ({
          name: `${client.name} ${client.lastName}`.trim(),
          deviceType: devices.find((device) => device.clientId === client.id)?.deviceType || '-'
        }));
        this.recentDevices = latestDevices;

        const latestDeliveredRepairs = repairs
          .filter((repair) => repair.status === 'RETIRADA')
          .sort((left, right) => new Date(right.returnDateTime || right.receiveDateTime || 0).getTime() - new Date(left.returnDateTime || left.receiveDateTime || 0).getTime())
          .slice(0, 5);

        this.recentRepairs = latestDeliveredRepairs.map((repair) => ({
          date: (repair.returnDateTime || repair.receiveDateTime) ? new Date(repair.returnDateTime || repair.receiveDateTime!).toLocaleDateString('es-AR') : '-',
          client: this.clientName(repair.idClient),
          price: this.asMoney(repair.price)
        }));

        this.buildBreakdowns();
        this.buildInactiveClients();
        this.refreshCharts();
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
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
      labels: this.monthlyRevenueRows.map((row) => row.label),
      datasets: [
        {
          label: 'Ingresos',
          data: this.monthlyRevenueRows.map((row) => row.value),
          backgroundColor: colorWithAlpha(brandColor, 0.75),
          borderColor: brandColor,
          borderWidth: 1.5,
          borderRadius: 8,
          maxBarThickness: 36
        }
      ]
    };

    this.deviceTypeChartData = {
      labels: this.deviceTypeRows.map((row) => row.label),
      datasets: [
        {
          data: this.deviceTypeRows.map((row) => row.value),
          backgroundColor: palette.slice(0, Math.max(this.deviceTypeRows.length, 1)),
          borderColor: this.themeMode === 'dark' ? '#171b1f' : '#ffffff',
          borderWidth: 2
        }
      ]
    };

    this.repairStatusChartData = {
      labels: this.repairStatusRows.map((row) => row.label),
      datasets: [
        {
          data: this.repairStatusRows.map((row) => row.value),
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

  private buildInactiveClients(): void {
    const byDevice = new Map<string, Date>();
    this.repairs.forEach((repair) => {
      if (!repair.idDevice || !repair.receiveDateTime) return;
      const date = new Date(repair.receiveDateTime);
      const current = byDevice.get(repair.idDevice);
      if (!current || date > current) {
        byDevice.set(repair.idDevice, date);
      }
    });

    const devicesById = new Map(this.devices.filter((device) => !!device.id).map((device) => [device.id!, device]));

    this.inactiveClients = Array.from(byDevice.entries())
      .map(([deviceId, lastDate]) => {
        const device = devicesById.get(deviceId);
        const client = device ? this.clients.find((item) => item.id === device.clientId) : undefined;
        const deviceLabel = device
          ? `${device.deviceType || 'Equipo'} ${device.brand || ''} ${device.model || ''}`.replace(/\s+/g, ' ').trim()
          : deviceId;
        const ownerLabel = client ? `${client.name} ${client.lastName}`.trim() : 'Cliente sin datos';
        return {
          name: `${deviceLabel} · ${ownerLabel}`,
          lastRepair: lastDate.toLocaleDateString('es-AR'),
          order: lastDate.getTime()
        };
      })
      .sort((left, right) => left.order - right.order)
      .slice(0, 5)
      .map(({ name, lastRepair }) => ({ name, lastRepair }));
  }

  private buildBreakdowns(): void {
    const deviceMap = new Map<string, number>();
    this.devices.forEach((device) => deviceMap.set(device.deviceType, (deviceMap.get(device.deviceType) || 0) + 1));

    const repairMap = new Map<string, number>();
    this.repairs.forEach((repair) => repairMap.set(repair.status, (repairMap.get(repair.status) || 0) + 1));

    const monthlyIncomeMap = new Map<string, number>();
    this.repairs.forEach((repair) => {
      if (!repair.receiveDateTime) return;
      const date = new Date(repair.receiveDateTime);
      const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
      monthlyIncomeMap.set(monthKey, (monthlyIncomeMap.get(monthKey) || 0) + this.asMoney(repair.price));
    });

    const sortedMonths = Array.from(monthlyIncomeMap.keys()).sort();
    this.monthRevenue = sortedMonths.length ? (monthlyIncomeMap.get(sortedMonths[sortedMonths.length - 1]) || 0) : 0;
    this.monthlyRevenueRows = sortedMonths
      .slice(-6)
      .map((month) => ({ label: this.formatMonth(month), value: monthlyIncomeMap.get(month) || 0 }));

    this.deviceTypeRows = Array.from(deviceMap.entries())
      .map(([label, value]) => ({ label, value }))
      .sort((left, right) => right.value - left.value);

    this.repairStatusRows = Array.from(repairMap.entries())
      .map(([label, value]) => ({ label: this.statusLabel(label as Repair['status']), value }))
      .sort((left, right) => right.value - left.value);
  }

  private clientName(clientId: string): string {
    const client = this.clients.find((item) => item.id === clientId);
    return client ? `${client.name} ${client.lastName}`.trim() : clientId;
  }

  private asMoney(value: unknown): number {
    const parsed = Number(value ?? 0);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  private formatMonth(value: string): string {
    const [year, month] = value.split('-').map(Number);
    if (!year || !month) return value;
    return new Date(year, month - 1, 1).toLocaleDateString('es-AR', { month: 'short', year: 'numeric' });
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
