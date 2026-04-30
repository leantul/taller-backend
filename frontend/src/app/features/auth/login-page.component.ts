import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { Router } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { MessageService } from 'primeng/api';
import { ThemeMode, ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule, InputTextModule, PasswordModule, ButtonModule, CardModule],
  template: `
    <section class="auth-shell">
      <p-card class="auth-card modern-auth">
        <img [src]="themeMode === 'dark' ? '/assets/logo-dark.png' : '/assets/logo-light.png'" alt="Logo" class="login-logo" />
        <form (ngSubmit)="submit()" class="p-fluid auth-form">
          <div class="field auth-field"><label>Usuario</label><input pInputText [(ngModel)]="username" name="username" required /></div>
          <div class="field auth-field"><label>Contraseña</label><p-password [(ngModel)]="password" name="password" [feedback]="false" [toggleMask]="true" [style]="{ width: '100%' }" inputStyleClass="auth-password-input" required></p-password></div>
          <button pButton type="submit" label="Entrar" icon="pi pi-sign-in"></button>
        </form>
        @if (error) { <small class="error"><i class="pi pi-exclamation-circle"></i> {{ error }}</small> }
      </p-card>
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
