import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-change-password-page',
  standalone: true,
  imports: [FormsModule],
  template: `
    <h2>Cambiar contraseña</h2>
    <form (ngSubmit)="submit()">
      <label>Contraseña actual</label>
      <input [(ngModel)]="currentPassword" name="currentPassword" type="password" required />

      <label>Nueva contraseña</label>
      <input [(ngModel)]="newPassword" name="newPassword" type="password" required />

      <button type="submit">Actualizar</button>
    </form>
    @if (message) {
      <p>{{ message }}</p>
    }
  `
})
export class ChangePasswordPageComponent {
  currentPassword = '';
  newPassword = '';
  message = '';

  constructor(private readonly auth: AuthService) {}

  submit(): void {
    this.auth.changePassword(this.currentPassword, this.newPassword).subscribe({
      next: () => (this.message = 'Contraseña actualizada correctamente.'),
      error: () => (this.message = 'No se pudo actualizar la contraseña.')
    });
  }
}
