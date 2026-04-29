import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { RouterLink, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './core/auth/auth.service';
import { MenubarModule } from 'primeng/menubar';
import { MenuModule } from 'primeng/menu';
import { ButtonModule } from 'primeng/button';
import { AvatarModule } from 'primeng/avatar';
import { MenuItem } from 'primeng/api';
import { ThemeMode, ThemeService } from './core/services/theme.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule, MenubarModule, MenuModule, ButtonModule, AvatarModule],
  template: `
    <main class="app-shell">
      @if (auth.isLoggedIn()) {
        <p-menubar [model]="navItems" styleClass="mb-4 app-menubar">
          <ng-template #start>
            <h1 class="brand-title">Taller de Reparaciones</h1>
          </ng-template>
          <ng-template #end>
            <div class="user-menu-wrap">
              <button pButton type="button" class="p-button-text p-button-rounded" [icon]="themeMode === 'dark' ? 'pi pi-sun' : 'pi pi-moon'" (click)="toggleTheme()"></button>
              <button pButton type="button" class="p-button-text" (click)="userMenu.toggle($event)">
                <p-avatar icon="pi pi-user" shape="circle"></p-avatar>
                <span>{{ username }}</span>
              </button>
              <p-menu #userMenu [popup]="true" [model]="userItems"></p-menu>
            </div>
          </ng-template>
        </p-menubar>
      }

      <router-outlet></router-outlet>
    </main>
  `
})
export class AppComponent {
  themeMode: ThemeMode;
  navItems: MenuItem[] = [
    { label: 'Dashboard', icon: 'pi pi-home', routerLink: '/' },
    { label: 'Clientes', icon: 'pi pi-users', routerLink: '/clientes' },
    { label: 'Dispositivos', icon: 'pi pi-desktop', routerLink: '/dispositivos' },
    { label: 'Reparaciones', icon: 'pi pi-wrench', routerLink: '/reparaciones' },
    { label: 'Notificaciones', icon: 'pi pi-bell', routerLink: '/notificaciones' }
  ];

  userItems: MenuItem[] = [
    { label: 'Cambiar contraseña', icon: 'pi pi-key', command: () => this.router.navigate(['/cambiar-password']) },
    { separator: true },
    { label: 'Salir', icon: 'pi pi-sign-out', command: () => this.auth.logout() }
  ];

  get username(): string {
    return localStorage.getItem('username') || 'Usuario';
  }

  constructor(public readonly auth: AuthService, private readonly router: Router, private readonly themeService: ThemeService) {
    this.themeMode = this.themeService.initTheme();
  }

  toggleTheme(): void {
    this.themeMode = this.themeService.toggleTheme(this.themeMode);
  }
}
