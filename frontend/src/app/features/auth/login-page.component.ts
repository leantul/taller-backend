import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { Router } from '@angular/router';
import { MessageService } from 'primeng/api';
import { ThemeMode, ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="auth-shell">
      <div class="auth-card">
        <button class="icon-button login-theme-toggle" type="button" [attr.aria-label]="themeMode === 'dark' ? 'Usar tema claro' : 'Usar tema oscuro'" (click)="toggleTheme()">
          <i [class]="themeMode === 'dark' ? 'pi pi-sun' : 'pi pi-moon'"></i>
        </button>

        <img [src]="themeMode === 'dark' ? '/assets/logo-dark.png' : '/assets/logo-light.png'" alt="Logo" class="login-logo" />

        <form (ngSubmit)="submit()" class="auth-form">
          <label class="field auth-field" for="login-username">
            <span>Usuario</span>
            <input id="login-username" class="control" [(ngModel)]="username" name="username" autocomplete="username" required />
          </label>
          <label class="field auth-field" for="login-password">
            <span>Contraseña</span>
            <input id="login-password" class="control" [(ngModel)]="password" name="password" type="password" autocomplete="current-password" required />
          </label>
          <button class="primary-button" type="submit">
            <i class="pi pi-sign-in"></i>
            <span>Entrar</span>
          </button>
        </form>
        @if (error) { <small class="error"><i class="pi pi-exclamation-circle"></i> {{ error }}</small> }
      </div>
    </section>
  `
})
export class LoginPageComponent {
  themeMode: ThemeMode;
  username = '';
  password = '';
  error = '';
  constructor(private readonly auth: AuthService, private readonly router: Router, private readonly messageService: MessageService, private readonly themeService: ThemeService) {
    this.themeMode = this.themeService.initTheme();
  }
  toggleTheme(): void {
    this.themeMode = this.themeService.toggleTheme(this.themeMode);
  }

  submit(): void {
    this.auth.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => {
        this.error = 'Credenciales inválidas';
        this.messageService.add({ severity: 'error', summary: 'Login inválido', detail: 'Usuario o contraseña incorrectos.' });
      }
    });
  }
}
