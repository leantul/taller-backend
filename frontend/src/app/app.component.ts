import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from './core/auth/auth.service';
import { ThemeMode, ThemeService } from './core/services/theme.service';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ToastModule } from 'primeng/toast';
import { DialogModule } from 'primeng/dialog';
import { MessageService } from 'primeng/api';
import { LoadingService } from './core/services/loading.service';
import { NotificationStateService } from './core/services/notification-state.service';
import { ErrorDialogService, ErrorDialogState } from './core/services/error-dialog.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, ProgressSpinnerModule, ToastModule, DialogModule],
  template: `
    <p-toast position="top-right"></p-toast>
    <p-dialog
      header="Detalle del error"
      [visible]="!!errorDialog"
      [modal]="true"
      [draggable]="false"
      [resizable]="false"
      [closable]="true"
      [style]="{ width: '52rem', maxWidth: '95vw' }"
      (visibleChange)="onErrorDialogVisibleChange($event)">
      @if (errorDialog) {
        <div class="error-dialog">
          <div class="error-dialog-header">
            <strong>{{ errorDialog.title }}</strong>
          </div>
          <pre class="error-dialog-detail">{{ errorDialog.detail }}</pre>
          <div class="error-dialog-actions">
            <button class="secondary-button" type="button" (click)="copyErrorDetail()">Copiar</button>
            <button class="primary-button" type="button" (click)="closeErrorDialog()">Cerrar</button>
          </div>
        </div>
      }
    </p-dialog>
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
                @if (item.path === '/notificaciones' && unreadNotifications > 0) {
                  <span class="nav-badge">{{ unreadNotifications }}</span>
                }
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
export class AppComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  themeMode: ThemeMode;
  unreadNotifications = 0;
  errorDialog: ErrorDialogState | null = null;
  navItems = [
    { label: 'Dashboard', icon: 'pi pi-home', path: '/', exact: true },
    { label: 'Clientes', icon: 'pi pi-users', path: '/clientes' },
    { label: 'Dispositivos', icon: 'pi pi-desktop', path: '/dispositivos' },
    { label: 'Reparaciones', icon: 'pi pi-wrench', path: '/reparaciones' },
    { label: 'Seguimientos', icon: 'pi pi-calendar-clock', path: '/seguimientos' },
    { label: 'Finanzas', icon: 'pi pi-chart-line', path: '/finanzas' },
    { label: 'Estados', icon: 'pi pi-th-large', path: '/status' },
    { label: 'Avisos', icon: 'pi pi-bell', path: '/notificaciones' },
    { label: 'Configuración', icon: 'pi pi-cog', path: '/configuracion' }
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


  constructor(
    public readonly auth: AuthService,
    private readonly router: Router,
    private readonly themeService: ThemeService,
    public readonly loadingService: LoadingService,
    private readonly notificationState: NotificationStateService,
    private readonly errorDialogService: ErrorDialogService,
    private readonly messageService: MessageService
  ) {
    this.themeMode = this.themeService.initTheme();
  }

  ngOnInit(): void {
    this.notificationState.unreadCount$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((count) => {
      this.unreadNotifications = count;
    });

    this.errorDialogService.state$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((state) => {
      this.errorDialog = state;
    });

    if (this.auth.isLoggedIn()) {
      this.notificationState.refreshUnreadCount();
    }

    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        if (this.auth.isLoggedIn()) {
          this.notificationState.refreshUnreadCount();
        }
      });
  }

  toggleTheme(): void { this.themeMode = this.themeService.toggleTheme(this.themeMode); }
  goToPassword(): void { this.router.navigate(['/cambiar-password']); }
  closeErrorDialog(): void { this.errorDialogService.close(); }
  onErrorDialogVisibleChange(visible: boolean): void { if (!visible) this.closeErrorDialog(); }

  copyErrorDetail(): void {
    const detail = this.errorDialog?.detail || '';
    if (!detail) {
      return;
    }

    navigator.clipboard.writeText(detail).then(() => {
      this.messageService.add({ severity: 'success', summary: 'Copiado', detail: 'El detalle del error se copió al portapapeles.' });
    }).catch(() => {
      this.messageService.add({ severity: 'warn', summary: 'No se pudo copiar', detail: 'Copiá el texto manualmente desde el diálogo.' });
    });
  }
}
