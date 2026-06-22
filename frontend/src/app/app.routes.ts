import { Routes } from '@angular/router';
import { DashboardPageComponent } from './features/dashboard/dashboard-page.component';
import { ClientsPageComponent } from './features/clients/clients-page.component';
import { DevicesPageComponent } from './features/devices/devices-page.component';
import { RepairsPageComponent } from './features/repairs/repairs-page.component';
import { NotificationsPageComponent } from './features/notifications/notifications-page.component';
import { LoginPageComponent } from './features/auth/login-page.component';
import { StatusPageComponent } from './features/status/status-page.component';
import { ChangePasswordPageComponent } from './features/auth/change-password-page.component';
import { FinancePageComponent } from './features/finance/finance-page.component';
import { WorkshopSettingsPageComponent } from './features/workshop/workshop-settings-page.component';
import { authGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginPageComponent },
  { path: '', component: DashboardPageComponent, canActivate: [authGuard] },
  { path: 'clientes', component: ClientsPageComponent, canActivate: [authGuard] },
  { path: 'dispositivos', component: DevicesPageComponent, canActivate: [authGuard] },
  { path: 'reparaciones', component: RepairsPageComponent, canActivate: [authGuard] },
  { path: 'finanzas', component: FinancePageComponent, canActivate: [authGuard] },
  { path: 'notificaciones', component: NotificationsPageComponent, canActivate: [authGuard] },
  { path: 'status', component: StatusPageComponent, canActivate: [authGuard] },
  { path: 'taller', component: WorkshopSettingsPageComponent, canActivate: [authGuard] },
  { path: 'cambiar-password', component: ChangePasswordPageComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: '' }
];
