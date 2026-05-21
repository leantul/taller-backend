import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-change-password-page',
  standalone: true,
  imports: [FormsModule],
  template: `
    <section class="page-heading">
      <div>
        <span class="eyebrow">Cuenta</span>
        <h1>Cambiar contraseña</h1>
      </div>
      <p>Actualizá la clave de acceso sin salir del panel.</p>
    </section>

    <section class="settings-shell">
      <form class="settings-form" (ngSubmit)="submit()">
        <label class="field" for="currentPassword">
          <span>Contraseña actual</span>
          <input id="currentPassword" class="control" [(ngModel)]="currentPassword" name="currentPassword" type="password" autocomplete="current-password" required />
        </label>

        <label class="field" for="newPassword">
          <span>Nueva contraseña</span>
          <input id="newPassword" class="control" [(ngModel)]="newPassword" name="newPassword" type="password" autocomplete="new-password" required />
        </label>

        <button class="primary-button" type="submit">
          <i class="pi pi-key"></i>
          <span>Actualizar contraseña</span>
        </button>

        @if (message) {
          <p class="inline-message">{{ message }}</p>
        }
      </form>
    </section>
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
