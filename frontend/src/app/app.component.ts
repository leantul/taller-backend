import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './core/auth/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule],
  template: `
    <main class="app-shell">
      <header>
        <h1>Taller de Reparaciones</h1>
        <p>Release 2.0.0 · Base inicial del frontend</p>
      </header>
      <nav *ngIf="auth.isLoggedIn()">
        <a routerLink="/">Dashboard</a>
        <a routerLink="/clientes">Clientes</a>
        <a routerLink="/dispositivos">Dispositivos</a>
        <a routerLink="/reparaciones">Reparaciones</a>
        <a routerLink="/notificaciones">Notificaciones</a>
        <a routerLink="/cambiar-password">Cambiar contraseña</a>
        <button type="button" (click)="auth.logout()">Salir</button>
      </nav>
      <router-outlet></router-outlet>
    </main>
  `
})
export class AppComponent {
  constructor(public readonly auth: AuthService) {}
}
