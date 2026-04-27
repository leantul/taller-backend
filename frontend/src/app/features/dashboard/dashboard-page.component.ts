import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  template: `
    <section class="dashboard">
      <h2>Dashboard</h2>
      <p>Inicio de implementación del panel principal.</p>
      <ul>
        <li>Recaudación mensual</li>
        <li>Costos mensuales</li>
        <li>Ganancia mensual</li>
        <li>Top 5 clientes inactivos</li>
      </ul>
    </section>
  `
})
export class DashboardPageComponent {}
