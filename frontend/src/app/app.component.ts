import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './core/auth/auth.service';
import { ThemeMode, ThemeService } from './core/services/theme.service';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ToastModule } from 'primeng/toast';
import { LoadingService } from './core/services/loading.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, ProgressSpinnerModule, ToastModule],
  template: `
    <p-toast position="top-right"></p-toast>
    <main class="app-shell" [class.is-loading]="(loadingService.loading$ | async) !== 0" [class.is-authenticated]="auth.isLoggedIn()">
      @if ((loadingService.loading$ | async) !== 0) {
        <div class="global-loading-overlay">
          <p-progressSpinner strokeWidth="5" ariaLabel="Cargando"></p-progressSpinner>
        </div>
      }
      @if (auth.isLoggedIn()) {
        <aside class="app-sidebar">
          <a class="brand-lockup" routerLink="/">
            <img class="brand-logo" [src]="themeMode === 'dark' ? '/assets/logo-dark.png' : '/assets/logo-light.png'" alt="Logo Taller" />
          </a>

          <nav class="app-nav" aria-label="Principal">
            @for (item of navItems; track item.path) {
              <a [routerLink]="item.path" routerLinkActive="is-active" [routerLinkActiveOptions]="item.exact ? { exact: true } : { exact: false }">
                <i [class]="item.icon"></i>
                <span>{{ item.label }}</span>
              </a>
            }
          </nav>

          <div class="sidebar-footer">
            <button class="icon-button" type="button" [attr.aria-label]="themeMode === 'dark' ? 'Usar tema claro' : 'Usar tema oscuro'" (click)="toggleTheme()">
              <i [class]="themeMode === 'dark' ? 'pi pi-sun' : 'pi pi-moon'"></i>
            </button>
            <button class="user-button" type="button" (click)="goToPassword()">
              <span class="avatar-dot">{{ usernameInitial }}</span>
              <span>{{ username }}</span>
            </button>
            <button class="icon-button" type="button" aria-label="Salir" (click)="auth.logout()">
              <i class="pi pi-sign-out"></i>
            </button>
          </div>
        </aside>
      }

      <section class="app-content">
        <router-outlet></router-outlet>
      </section>
    </main>
  `
})
export class AppComponent {
  themeMode: ThemeMode;
  navItems = [
    { label: 'Dashboard', icon: 'pi pi-home', path: '/', exact: true },
    { label: 'Clientes', icon: 'pi pi-users', path: '/clientes' },
    { label: 'Dispositivos', icon: 'pi pi-desktop', path: '/dispositivos' },
    { label: 'Reparaciones', icon: 'pi pi-wrench', path: '/reparaciones' },
    { label: 'Finanzas', icon: 'pi pi-chart-line', path: '/finanzas' },
    { label: 'Estados', icon: 'pi pi-th-large', path: '/status' },
    { label: 'Avisos', icon: 'pi pi-bell', path: '/notificaciones' }
  ];
  get username(): string { return localStorage.getItem('fullName') || this.getNameFromToken() || localStorage.getItem('username') || 'Usuario'; }
  get usernameInitial(): string { return this.username.trim().charAt(0).toUpperCase() || 'U'; }

  private parseJwtPayload(token: string): Record<string, unknown> {
    const base64Url = token.split('.')[1] || "";
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64 + "=".repeat((4 - (base64.length % 4)) % 4);
    return JSON.parse(atob(padded));
  }

  private getNameFromToken(): string | null {
    const token = localStorage.getItem('token');
    if (!token) return null;
    try {
      const payload = this.parseJwtPayload(token);
      return (payload['fullName'] as string) || (payload['name'] as string) || (payload['preferred_username'] as string) || null;
    } catch {
      return null;
    }
  }


  constructor(public readonly auth: AuthService, private readonly router: Router, private readonly themeService: ThemeService, public readonly loadingService: LoadingService) {
    this.themeMode = this.themeService.initTheme();
  }
  toggleTheme(): void { this.themeMode = this.themeService.toggleTheme(this.themeMode); }
  goToPassword(): void { this.router.navigate(['/cambiar-password']); }
}
