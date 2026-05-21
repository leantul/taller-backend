import { Component } from '@angular/core';

@Component({
  selector: 'app-notifications-page',
  standalone: true,
  template: `
    <section class="page-heading">
      <div>
        <span class="eyebrow">Seguimiento</span>
        <h1>Notificaciones</h1>
      </div>
      <p>Esta vista sigue en estado base. La dejamos alineada con el resto del frontend para completar luego la logica de leido y no leido.</p>
    </section>

    <section class="placeholder-panel">
      <div class="placeholder-icon">
        <i class="pi pi-bell"></i>
      </div>
      <div class="placeholder-copy">
        <strong>Centro de avisos</strong>
        <p>Cuando se implemente la funcionalidad, este espacio puede mostrar retiros pendientes, presupuestos sin respuesta y recordatorios al cliente.</p>
      </div>
    </section>
  `
})
export class NotificationsPageComponent {}
