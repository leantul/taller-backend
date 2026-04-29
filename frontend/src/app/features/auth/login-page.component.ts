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

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule, InputTextModule, PasswordModule, ButtonModule, CardModule],
  template: `
    <section class="auth-shell">
      <p-card class="auth-card modern-auth" header="Bienvenido">
        <img src="/assets/logo-light.png" alt="Logo" class="login-logo" />
        <p class="auth-subtitle">Ingresá para gestionar el taller.</p>
        <form (ngSubmit)="submit()" class="p-fluid">
          <div class="field"><label>Usuario</label><input pInputText [(ngModel)]="username" name="username" required /></div>
          <div class="field"><label>Contraseña</label><p-password [(ngModel)]="password" name="password" [feedback]="false" [toggleMask]="true" required></p-password></div>
          <button pButton type="submit" label="Entrar" icon="pi pi-sign-in"></button>
        </form>
        @if (error) { <small class="error"><i class="pi pi-exclamation-circle"></i> {{ error }}</small> }
      </p-card>
    </section>
  `
})
export class LoginPageComponent {
  username = '';
  password = '';
  error = '';
  constructor(private readonly auth: AuthService, private readonly router: Router, private readonly messageService: MessageService) {}
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
