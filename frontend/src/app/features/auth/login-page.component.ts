import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="auth-card">
      <h2>Ingresar</h2>
      <form (ngSubmit)="submit()">
        <label>Usuario</label>
        <input [(ngModel)]="username" name="username" required />

        <label>Contraseña</label>
        <input [(ngModel)]="password" name="password" type="password" required />

        <button type="submit">Entrar</button>
      </form>
      @if (error) {
        <p class="error">{{ error }}</p>
      }
    </section>
  `
})
export class LoginPageComponent {
  username = 'admin';
  password = 'Admin1234!';
  error = '';

  constructor(private readonly auth: AuthService, private readonly router: Router) {}

  submit(): void {
    this.auth.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(['/']),
      error: () => (this.error = 'Credenciales inválidas')
    });
  }
}
