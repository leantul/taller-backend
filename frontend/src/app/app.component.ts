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

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, CommonModule, MenubarModule, MenuModule, ButtonModule, AvatarModule],
  template: `
    <main class="app-shell">
      <header class="app-header">
        <h1>Taller de Reparaciones</h1>
      </header>

      @if (auth.isLoggedIn()) {
        <p-menubar [model]="navItems" styleClass="mb-4">
          <ng-template #end>
            <div class="user-menu-wrap">
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

  constructor(public readonly auth: AuthService, private readonly router: Router) {}
}
