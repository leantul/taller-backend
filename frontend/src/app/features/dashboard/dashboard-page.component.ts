import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CardModule } from 'primeng/card';
import { ChartModule } from 'primeng/chart';
import { ApiService } from '../../core/services/api.service';
import { Client } from '../../shared/models/client.model';
import { Device } from '../../shared/models/device.model';
import { Repair } from '../../shared/models/repair.model';

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
      <p-card header="Evolución mensual de ingresos" class="chart-card" (click)="toggleChart('income')"><p-chart type="line" [data]="monthlyIncomeChart" [options]="getChartOptions('income')"></p-chart></p-card>
      <p-card header="Equipos por tipo" class="chart-card" (click)="toggleChart('devices')"><p-chart type="bar" [data]="devicesByTypeChart" [options]="getChartOptions('devices')"></p-chart></p-card>
      <p-card header="Reparaciones por estado" class="chart-card" (click)="toggleChart('repairs')"><p-chart type="doughnut" [data]="repairsByStatusChart" [options]="getChartOptions('repairs')"></p-chart></p-card>
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
export class DashboardPageComponent implements OnInit {
  clients: Client[] = [];
  devices: Device[] = [];
  repairs: Repair[] = [];
  totalRevenue = 0;
  monthRevenue = 0;
  recentClients: { name: string; deviceType: string }[] = [];
  recentDevices: Device[] = [];
  recentRepairs: { date: string; client: string; price: number }[] = [];
  inactiveClients: { name: string; lastRepair: string | null }[] = [];
  devicesByTypeChart: any;
  repairsByStatusChart: any;
  monthlyIncomeChart: any;
  chartOptions: any = { plugins: { legend: { labels: { color: '#94a3b8' } } }, maintainAspectRatio: false };
  expandedChart: 'income' | 'devices' | 'repairs' | null = null;
  waitingPickupCount = 0;
  inProgressCount = 0;
  quotedPendingCount = 0;

  constructor(private readonly api: ApiService, private readonly changeDetector: ChangeDetectorRef) {}

  ngOnInit(): void {
    forkJoin({ clients: this.api.getClients(), devices: this.api.getDevices(), repairs: this.api.getRepairs(), latestClients: this.api.getLatestClients(), latestDevices: this.api.getLatestDevices(), latestRepairs: this.api.getLatestRepairs() }).subscribe(({ clients, devices, repairs, latestClients, latestDevices, latestRepairs }) => {
      this.clients = clients;
      this.devices = devices;
      this.repairs = repairs;
      this.totalRevenue = repairs.reduce((acc, item) => acc + this.asMoney(item.price), 0);
      this.waitingPickupCount = repairs.filter((item) => item.status === 'ESPERANDO_RETIRO').length;
      this.inProgressCount = repairs.filter((item) => item.status === 'HACIENDO' || item.status === 'RECIBIDA').length;
      this.quotedPendingCount = repairs.filter((item) => item.status === 'PRESUPUESTADA_ESPERANDO_RESPUESTA').length;

      this.recentClients = latestClients.map((c) => ({
          name: `${c.name} ${c.lastName}`.trim(),
          deviceType: devices.find((d) => d.clientId === c.id)?.deviceType || '-'
        }));
      this.recentDevices = latestDevices;
      const latestDeliveredRepairs = repairs
        .filter((r) => r.status === 'RETIRADA')
        .sort((a, b) => new Date(b.returnDateTime || b.receiveDateTime || 0).getTime() - new Date(a.returnDateTime || a.receiveDateTime || 0).getTime())
        .slice(0, 5);
      this.recentRepairs = latestDeliveredRepairs.map((r) => ({
        date: (r.returnDateTime || r.receiveDateTime) ? new Date(r.returnDateTime || r.receiveDateTime!).toLocaleDateString('es-AR') : '-',
        client: this.clients.find((c) => c.id === r.idClient) ? `${this.clients.find((c) => c.id === r.idClient)!.name} ${this.clients.find((c) => c.id === r.idClient)!.lastName}` : r.idClient,
        price: r.price || 0
      }));

      this.buildCharts();
      this.buildInactiveClients();
      this.changeDetector.detectChanges();
    });
  }

  private buildInactiveClients(): void {
    const byDevice = new Map<string, Date>();
    this.repairs.forEach((r) => {
      if (!r.idDevice || !r.receiveDateTime) return;
      const date = new Date(r.receiveDateTime);
      const current = byDevice.get(r.idDevice);
      if (!current || date > current) byDevice.set(r.idDevice, date);
    });

    const devicesById = new Map(this.devices.filter((d) => !!d.id).map((d) => [d.id!, d]));

    this.inactiveClients = Array.from(byDevice.entries())
      .map(([deviceId, lastDate]) => {
        const device = devicesById.get(deviceId);
        const client = device ? this.clients.find((c) => c.id === device.clientId) : undefined;
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
      .sort((a, b) => a.order - b.order)
      .slice(0, 5)
      .map(({ name, lastRepair }) => ({ name, lastRepair }));
  }

  toggleChart(chart: 'income' | 'devices' | 'repairs'): void { this.expandedChart = this.expandedChart === chart ? null : chart; }
  getChartOptions(chart: 'income' | 'devices' | 'repairs'): any { return { ...this.chartOptions, aspectRatio: this.expandedChart === chart ? 1.2 : 2.2 }; }

  private buildCharts(): void {
    const deviceMap = new Map<string, number>();
    this.devices.forEach((item) => deviceMap.set(item.deviceType, (deviceMap.get(item.deviceType) || 0) + 1));
    const repairMap = new Map<string, number>();
    this.repairs.forEach((item) => repairMap.set(item.status, (repairMap.get(item.status) || 0) + 1));

    const monthlyIncomeMap = new Map<string, number>();
    this.repairs.forEach((item) => {
      if (!item.receiveDateTime) return;
      const date = new Date(item.receiveDateTime);
      const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
      monthlyIncomeMap.set(monthKey, (monthlyIncomeMap.get(monthKey) || 0) + this.asMoney(item.price));
    });
    const sortedMonths = Array.from(monthlyIncomeMap.keys()).sort();
    this.monthRevenue = sortedMonths.length ? (monthlyIncomeMap.get(sortedMonths[sortedMonths.length - 1]) || 0) : 0;
    this.monthlyIncomeChart = {
      labels: sortedMonths,
      datasets: [{ label: 'Ingresos (ARS)', data: sortedMonths.map((month) => monthlyIncomeMap.get(month) || 0), borderColor: '#34b6f8', backgroundColor: 'rgba(52,182,248,0.2)', tension: 0.3, fill: true }]
    };
    this.devicesByTypeChart = { labels: Array.from(deviceMap.keys()), datasets: [{ label: 'Equipos', backgroundColor: '#0ea5e9', data: Array.from(deviceMap.values()) }] };
    this.repairsByStatusChart = { labels: Array.from(repairMap.keys()), datasets: [{ data: Array.from(repairMap.values()), backgroundColor: ['#0ea5e9', '#22c55e', '#f59e0b', '#ef4444', '#6366f1', '#14b8a6'] }] };
  }

  private asMoney(value: unknown): number {
    const parsed = Number(value ?? 0);
    return Number.isFinite(parsed) ? parsed : 0;
  }
}
