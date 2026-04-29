import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { CardModule } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ChartModule } from 'primeng/chart';
import { ApiService } from '../../core/services/api.service';
import { Client } from '../../shared/models/client.model';
import { Device } from '../../shared/models/device.model';
import { Repair } from '../../shared/models/repair.model';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [CommonModule, CardModule, TableModule, TagModule, ChartModule],
  template: `
    <section class="dashboard-grid">
      <p-card header="Clientes" subheader="Total">
        <div class="metric">{{ clients.length }}</div>
      </p-card>
      <p-card header="Dispositivos" subheader="Total">
        <div class="metric">{{ devices.length }}</div>
      </p-card>
      <p-card header="Reparaciones" subheader="Total">
        <div class="metric">{{ repairs.length }}</div>
      </p-card>
      <p-card header="Ingresos estimados" subheader="Suma de reparaciones">
        <div class="metric">{{ totalRevenue | currency:'USD' }}</div>
      </p-card>
    </section>

    <section class="dashboard-grid charts">
      <p-card header="Equipos por tipo">
        <p-chart type="bar" [data]="devicesByTypeChart" [options]="chartOptions"></p-chart>
      </p-card>
      <p-card header="Reparaciones por estado">
        <p-chart type="doughnut" [data]="repairsByStatusChart" [options]="chartOptions"></p-chart>
      </p-card>
    </section>

    <section class="dashboard-grid lists">
      <p-card header="Últimos 5 clientes">
        <p-table [value]="recentClients" size="small" [tableStyle]="{ 'min-width': '24rem' }">
          <ng-template pTemplate="header"><tr><th>Nombre</th><th>DNI</th></tr></ng-template>
          <ng-template pTemplate="body" let-item><tr><td>{{ item.name }} {{ item.lastName }}</td><td>{{ item.dni }}</td></tr></ng-template>
        </p-table>
      </p-card>

      <p-card header="Últimos 5 dispositivos">
        <p-table [value]="recentDevices" size="small">
          <ng-template pTemplate="header"><tr><th>Tipo</th><th>Marca</th><th>Modelo</th></tr></ng-template>
          <ng-template pTemplate="body" let-item><tr><td>{{ item.deviceType }}</td><td>{{ item.brand }}</td><td>{{ item.model }}</td></tr></ng-template>
        </p-table>
      </p-card>

      <p-card header="Últimas 5 reparaciones">
        <p-table [value]="recentRepairs" size="small">
          <ng-template pTemplate="header"><tr><th>Orden</th><th>Estado</th><th>Monto</th></tr></ng-template>
          <ng-template pTemplate="body" let-item><tr><td>{{ item.orderNumber }}</td><td><p-tag [value]="item.status"></p-tag></td><td>{{ item.price | currency:'USD' }}</td></tr></ng-template>
        </p-table>
      </p-card>
          <p-card header="Top 5 clientes inactivos">
        <p-table [value]="inactiveClients" size="small">
          <ng-template pTemplate="header"><tr><th>Cliente</th><th>Última reparación</th></tr></ng-template>
          <ng-template pTemplate="body" let-item><tr><td>{{ item.name }}</td><td>{{ item.lastRepair || 'Sin historial' }}</td></tr></ng-template>
        </p-table>
      </p-card>
    </section>
  `
})
export class DashboardPageComponent implements OnInit {
  clients: Client[] = [];
  devices: Device[] = [];
  repairs: Repair[] = [];
  totalRevenue = 0;

  recentClients: Client[] = [];
  recentDevices: Device[] = [];
  recentRepairs: Repair[] = [];
  inactiveClients: { name: string; lastRepair: string | null }[] = [];

  devicesByTypeChart: any;
  repairsByStatusChart: any;
  chartOptions: any = { plugins: { legend: { labels: { color: '#334155' } } } };

  constructor(private readonly api: ApiService) {}

  ngOnInit(): void {
    forkJoin({
      clients: this.api.getClients(),
      devices: this.api.getDevices(),
      repairs: this.api.getRepairs()
    }).subscribe(({ clients, devices, repairs }) => {
      this.clients = clients;
      this.devices = devices;
      this.repairs = repairs;
      this.totalRevenue = repairs.reduce((acc, item) => acc + (item.price || 0), 0);

      this.recentClients = [...clients].slice(-5).reverse();
      this.recentDevices = [...devices].slice(-5).reverse();
      this.recentRepairs = [...repairs].slice(-5).reverse();

      this.buildCharts();
      this.buildInactiveClients();
    });
  }

  private buildInactiveClients(): void {
    const byClient = new Map<string, Date>();
    this.repairs.forEach((r) => {
      if (!r.idClient) return;
      const date = r.receiveDateTime ? new Date(r.receiveDateTime) : new Date(0);
      const current = byClient.get(r.idClient);
      if (!current || date > current) byClient.set(r.idClient, date);
    });

    this.inactiveClients = this.clients
      .map((c) => ({
        name: `${c.name} ${c.lastName}`.trim(),
        lastRepair: byClient.get(c.id!) ? byClient.get(c.id!)!.toISOString().slice(0, 10) : null,
        order: byClient.get(c.id!) ? byClient.get(c.id!)!.getTime() : 0
      }))
      .sort((a, b) => a.order - b.order)
      .slice(0, 5)
      .map(({ name, lastRepair }) => ({ name, lastRepair }));
  }

  private buildCharts(): void {
    const deviceMap = new Map<string, number>();
    this.devices.forEach((item) => deviceMap.set(item.deviceType, (deviceMap.get(item.deviceType) || 0) + 1));

    const repairMap = new Map<string, number>();
    this.repairs.forEach((item) => repairMap.set(item.status, (repairMap.get(item.status) || 0) + 1));

    this.devicesByTypeChart = {
      labels: Array.from(deviceMap.keys()),
      datasets: [{ label: 'Equipos', backgroundColor: '#0ea5e9', data: Array.from(deviceMap.values()) }]
    };

    this.repairsByStatusChart = {
      labels: Array.from(repairMap.keys()),
      datasets: [{ data: Array.from(repairMap.values()), backgroundColor: ['#0ea5e9', '#22c55e', '#f59e0b', '#ef4444', '#6366f1', '#14b8a6'] }]
    };
  }
}
